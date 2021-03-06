package no.capraconsulting.chat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import no.capraconsulting.chatmessages.*;
import no.capraconsulting.config.PropertiesHelper;
import no.capraconsulting.enums.Chat;
import no.capraconsulting.enums.MixpanelEvent;
import no.capraconsulting.enums.Msg;
import no.capraconsulting.mixpanel.MixpanelService;
import no.capraconsulting.utils.ChatUtils;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static no.capraconsulting.utils.EndpointUtils.getActiveSubjects;

public class ChatEndpoint extends WebSocketAdapter {

    private static Logger LOG = LoggerFactory.getLogger(ChatEndpoint.class);
    // <subject, List<uniqueID>>
    static final ConcurrentMap<String, List<String>> queues = new ConcurrentHashMap<>();
    // <uniqueID, socket>
    static final ConcurrentMap<String, ChatEndpoint> sockets = new ConcurrentHashMap<>();
    // <roomID, List<uniqueID>>
    static final ConcurrentMap<String, List<String>> rooms = new ConcurrentHashMap<>();
    // <uniqueID, Student Info>
    static final ConcurrentMap<String, StudentInfo> waitingRooms = new ConcurrentHashMap<>();
    // <uniqueID, ClosedChat>
    static final ConcurrentMap<String, ClosedChat> reconnectList = new ConcurrentHashMap<>();

    public static final ConcurrentMap<String, Volunteer> activeVolunteers = new ConcurrentHashMap<>();

    public static AtomicInteger studentNicknameCounter = new AtomicInteger(1);

    private static int studentNicknameCounterMaxValue = PropertiesHelper.getIntProperty(
        PropertiesHelper.getProperties(),
        PropertiesHelper.NICKNAME_COUNTER_MAX,
        Integer.MAX_VALUE
    );

    // <uniqueID, Long> TODO: Sandra, et ConcurrentMap som holder på studentID og når student entret chat. Må tømmes når får timeout når leksehjelp er stengt, og student må fjernes når chat med student lukkes.
    static final ConcurrentMap<String, Long> studentsEnteredChat = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();
    private MixpanelService mixpanelService = new MixpanelService();

    private boolean volunteer = false;
    private Session session;
    private String id;

    public String getId() {
        return id;
    }

    private String generateID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onWebSocketText(String message) {
        LOG.info("Retrieved socket message with message: " + message);
        JsonParser parser = new JsonParser();
        JsonObject jsonMsg = (JsonObject) parser.parse(message);
        String payload = jsonMsg.get("payload").toString();
        JsonElement msgType = jsonMsg.get("msgType");
        Msg.MessageEnum type = gson.fromJson(msgType.toString(), Msg.MessageEnum.class);

        LOG.info(type.name());

        switch (type) {
            case TEXT:
                textMessageHandler(payload);
                break;
            case ENTER_QUEUE:
                enterQueueMessageHandler(payload);
                break;
            case UPDATE_QUEUE:
                updateQueueMessageHandler(payload);
                break;
            case GENERATE_ROOM:
                generateRoomMessageHandler(payload);
                break;
            case QUEUE_LIST:
                SocketMessage queueMessage = getQueueMessage();
                this.sendClient(ChatUtils.stringify(queueMessage));
                break;
            case JOIN_CHAT:
                addToChat(payload);
                break;
            case LEAVE_CHAT:
                removeFromChat(payload);
                break;
            case AVAILABLE_CHAT:
                getAvailableForChat(payload);
                break;
            case PING:
                LOG.info("Recieved ping");
                break;
            case RECONNECT:
                reconnectHandler(payload);
                break;
            case SET_VOLUNTEER:
                activeVolunteer(payload);
                break;
            case STUDENT_LEAVE:
                studentLeaveHandler(payload);
                break;
            case REMOVE_STUDENT_FROM_QUEUE:
                studentLeaveHandler(payload);
                break;
            default:
                break;
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        LOG.info("onWebSocketClose() called, status code: " + statusCode + ", reason: " + reason);
        if (ChatEndpoint.sockets.containsKey(this.id)) {
            // remove connection
            LOG.info("Disconnecting socket");
            LOG.info(this.id);
            ClosedChat closedChat = new ClosedChat(this);
            if (activeVolunteers.containsKey(this.id)) {
                Volunteer volunteer = activeVolunteers.remove(this.id);
                closedChat.setVolunteer(volunteer);
                sendUpdateActiveSubjects();
            }

            closedChat.run();
            reconnectList.put(this.id, closedChat);

            ChatEndpoint.sockets.remove(this.id);
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {

        this.session = session;
        this.id = generateID();

        ChatEndpoint.sockets.put(this.id, this);

        SocketMessage msg =
            SocketMessage.Builder.newInstance()
                .withMsgType(Msg.MessageEnum.CONNECTION)
                .withPayload(
                    new ActiveSubjectsMessage.Builder()
                        .withUniqueID(this.id)
                        .activeSubjects(getActiveSubjects())
                        .build()
                )
                .build();

        this.sendClient(ChatUtils.stringify(msg));
        LOG.info("New client connected");
        LOG.info(msg.toString());
    }

    static void updatePositionsInQueue() {
        ChatEndpoint.waitingRooms.forEach(
            (uid, si) -> {
                si.decrementPositionInQueue();
                StudentInfoMessage updateMessage =
                    new StudentInfoMessage.Builder().withStudentInfo(si).build();

                ChatEndpoint.sockets
                    .get(uid)
                    .sendClient(
                        ChatUtils.stringify(
                            SocketMessage.Builder.newInstance()
                                .withMsgType(Msg.MessageEnum.UPDATE_QUEUE)
                                .withPayload(updateMessage)
                                .build()));
            });
    }

    private void activeVolunteer(String msg) {
        Volunteer vol = gson.fromJson(msg, Volunteer.class);
        vol.setChatID(this.id);
        volunteer = true;
        activeVolunteers.put(vol.getChatID(), vol);
        LOG.info("The number of active volunteers is :" + activeVolunteers.size());
        sendUpdateActiveSubjects();
    }

    private void sendUpdateActiveSubjects() {
        LOG.info("Send the active subjects to all connected sockets");
        String message = ChatUtils.stringify(getActiveSubjectsMessage());
        ChatEndpoint.sockets.keySet().forEach(key -> ChatEndpoint.sockets.get(key).sendClient(
            message
        ));
    }

    private static SocketMessage getActiveSubjectsMessage() {
        return SocketMessage.Builder.newInstance()
            .withMsgType(Msg.MessageEnum.UPDATE_ACTIVE_SUBJECTS)
            .withPayload(
                new ActiveSubjectsMessage.Builder()
                    .activeSubjects(getActiveSubjects())
                    .build())
            .build();
    }

    private void textMessageHandler(String msg) {

        TextMessage partialTextMessage = gson.fromJson(msg, TextMessage.class);

        TextMessage textMessage =
            new TextMessage.Builder()
                .withFiles(partialTextMessage.getFiles())
                .withAuthor(partialTextMessage.getAuthor())
                .withMessage(partialTextMessage.getMessage())
                .withUniqueID(partialTextMessage.getUniqueID())
                .withRoomID(partialTextMessage.getRoomID())
                .build();

        if (!ChatEndpoint.rooms
            .get(partialTextMessage.getRoomID())
            .contains(partialTextMessage.getUniqueID())) {
            // User that sent the message is not in the room
            return;
        }

        for (String socketID : ChatEndpoint.rooms.get(partialTextMessage.getRoomID())) {

            if (socketID.equals(this.id)) {
                // skip me
                continue;
            }
            SocketMessage socketMessage =
                SocketMessage.Builder.newInstance()
                    .withMsgType(Msg.MessageEnum.TEXT)
                    .withPayload(textMessage)
                    .build();

            Optional.ofNullable(ChatEndpoint.sockets.get(socketID))
                .ifPresent(endpoint ->
                    endpoint.sendClient(ChatUtils.stringify(socketMessage))
                );
        }
    }

    private void updateQueueMessageHandler(String msg) {
        StudentInfo partialInfo = gson.fromJson(msg, StudentInfo.class);

        try {
            StudentInfo studentInfo = ChatEndpoint.waitingRooms.get(this.id);
            studentInfo.setIntroText(partialInfo.getIntroText());
            studentInfo.setGrade(partialInfo.getGrade());
            studentInfo.setThemes(partialInfo.getThemes());
            ChatEndpoint.waitingRooms.replace(this.id, studentInfo);
            ChatEndpoint.sockets
                .get(this.id)
                .sendClient(
                    ChatUtils.stringify(
                        SocketMessage.Builder.newInstance()
                            .withMsgType(Msg.MessageEnum.UPDATE_QUEUE)
                            .withPayload(
                                new StudentInfoMessage.Builder()
                                    .withStudentInfo(studentInfo)
                                    .build())
                            .build()));
            LOG.info("Student info updated");
            LOG.info(this.id);
        } catch (Error e) {
            LOG.error("Student is not already in queue");
            LOG.error(e.getMessage());
        }
    }

    private void enterQueueMessageHandler(String msg) {

        StudentInfo studentInfo = createClient(msg);

        // Add to hashMap to display queue positions
        ChatEndpoint.queues.putIfAbsent(studentInfo.getSubject(), new ArrayList<>());
        ChatEndpoint.queues.get(studentInfo.getSubject()).add(this.id);

        // +1 because this student is not currently in the waiting room
        studentInfo.setPositionInQueue(ChatEndpoint.waitingRooms.size() + 1);

        StudentInfoMessage infoMessage =
            new StudentInfoMessage.Builder().withStudentInfo(studentInfo).build();

        if (!ChatEndpoint.waitingRooms.containsKey(this.id)) {
            studentInfo.setTimePlacedInQueue(System.currentTimeMillis());
            ChatEndpoint.waitingRooms.put(this.id, studentInfo);
            SocketMessage confirmation =
                SocketMessage.Builder.newInstance()
                    .withMsgType(Msg.MessageEnum.CONFIRMED_QUEUE)
                    .withPayload(infoMessage)
                    .build();
            this.sendClient(ChatUtils.stringify(confirmation));

            mixpanelService.trackEventWithStudentInformation(MixpanelEvent.STUDENT_ENTERED_QUEUE, studentInfo);
            LOG.info("New student put in queue, queue length:");
            LOG.info(Integer.toString(ChatEndpoint.waitingRooms.size()));

            sendUpdateQueueMessageToVolunteers();
        }
    }

    private void generateRoomMessageHandler(String message) {

        RoomMessage payload = gson.fromJson(message, RoomMessage.class);

        String studentID = payload.getStudentID();

        StudentInfo studentInfo = ChatEndpoint.waitingRooms.remove(studentID);
        studentsEnteredChat.put(studentInfo.getUniqueID(), System.currentTimeMillis());

        List<String> al = new ArrayList<>();
        al.add(this.id);
        al.add(studentInfo.getUniqueID());
        String roomID = generateID();

        if (activeVolunteers.containsKey(this.id)) {
            try {
                activeVolunteers.get(this.id).setRoomID(roomID);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        RoomMessage.Builder payloadBuilder = new RoomMessage.Builder()
            .withUniqueID(payload.getUniqueID())
            .withRoomID(roomID)
            .withStudentID(studentID)
            .withVolName(payload.getVolName())
            .withVolunteerCount(1L);

        if ((studentInfo.getChatType() == Chat.ChatTypeEnum.LEKSEHJELP_VIDEO)
            || (studentInfo.getChatType() == Chat.ChatTypeEnum.MESTRING_VIDEO)) {
            // With talkyID
            String talkyID = generateID();
            payloadBuilder.withTalkyID(talkyID);
            LOG.info("Video Chat created, id: {}", talkyID);
        }

        SocketMessage returnMsg = SocketMessage.Builder.newInstance()
            .withMsgType(Msg.MessageEnum.DISTRIBUTE_ROOM)
            .withPayload(payloadBuilder.build())
            .build();

        mixpanelService.trackEventWithStudentInformation(MixpanelEvent.VOLUNTEER_STARTED_HELP, studentInfo);
        ChatEndpoint.rooms.put(roomID, al);
        LOG.info("New chat room created");

        for (String socketID : ChatEndpoint.rooms.get(roomID)) {
            ChatEndpoint.sockets.get(socketID).sendClient(ChatUtils.stringify(returnMsg));
        }

        // Update positions in queue
        List<String> queue = ChatEndpoint.queues.get(studentInfo.getSubject());
        queue.remove(studentID);

        ChatEndpoint.updatePositionsInQueue();
        sendUpdateQueueMessageToVolunteers();
    }

    private void addToChat(String msg) {

        RoomMessage payload = gson.fromJson(msg, RoomMessage.class);

        // Get withRoomID from payload
        String roomID = payload.getRoomID();
        // Get talkyID from payload
        String talkyID = payload.getTalkyID();
        // Get uniqueID from payload
        String uniqueID = payload.getUniqueID();

        List<String> room = ChatEndpoint.rooms.get(roomID);

        Optional<ChatEndpoint> op = Optional.ofNullable(ChatEndpoint.sockets.get(uniqueID));
        // Get volunteerID from payload

        op.ifPresent(volunteer -> {
            room.add(payload.getUniqueID());
            ChatEndpoint.rooms.put(roomID, room);
        });


        Volunteer reciever = activeVolunteers.get(uniqueID);
        reciever.setRoomID(payload.getRoomID());
        activeVolunteers.put(uniqueID, reciever);

        Volunteer sender = activeVolunteers.get(this.id);
        sender.setRoomID(payload.getRoomID());
        activeVolunteers.put(this.id, sender);

        SocketMessage returnMsg =
            SocketMessage.Builder.newInstance()
                .withMsgType(Msg.MessageEnum.JOIN_CHAT)
                .withPayload(
                    new RoomMessage.Builder()
                        .withUniqueID(payload.getUniqueID())
                        .withRoomID(roomID)
                        .withUniqueID(payload.getUniqueID())
                        .withTalkyID(talkyID)
                        .withStudentInfo(payload.getStudentInfo())
                        .withChatHistory(payload.getChatHistory())
                        .withVolunteerCount(sumVolunteersInRoom(roomID))
                        .build())
                .build();

        ChatEndpoint.sockets.get(uniqueID).sendClient(ChatUtils.stringify(returnMsg));
        ChatEndpoint.rooms.get(roomID).forEach(uid -> {
            ChatEndpoint.sockets.get(uid).sendClient(ChatUtils.stringify(
                SocketMessage.Builder.newInstance()
                    .withMsgType(Msg.MessageEnum.TEXT)
                    .withPayload(
                        new TextMessage.Builder()
                            .withUniqueID("NOTIFICATION")
                            .withRoomID(roomID)
                            .withMessage(String.format("%s har blitt med i chatten.", payload.getVolName()))
                            .build())
                    .build()
            ));
        });
    }

    private StudentInfo createClient(String message) {

        StudentInfo user = gson.fromJson(message, StudentInfo.class);
        user.setUniqueID(id);

        int nicknameCounter = studentNicknameCounter.getAndIncrement();
        if (nicknameCounter >= studentNicknameCounterMaxValue) {
            studentNicknameCounter.set(1);
        }
        user.setNickname(String.format("Elev #%s", nicknameCounter));

        return user;
    }

    private void removeFromChat(String message) {
        RoomMessage payload = gson.fromJson(message, RoomMessage.class);
        String roomID = payload.getRoomID();
        String uniqueID = payload.getUniqueID();
        Volunteer volunteer = ChatEndpoint.activeVolunteers.get(uniqueID);
        StudentInfo studentInfo = payload.getStudentInfo();

        try {
            List<String> room = ChatEndpoint.rooms.get(roomID);

            SocketMessage.Builder confirmationBuilder = SocketMessage.Builder.newInstance()
                .withMsgType(Msg.MessageEnum.LEAVE_CHAT);

            LeaveMessage.Builder payloadBuilder = new LeaveMessage.Builder()
                .withName(volunteer.getName())
                .withRoomID(roomID)
                .withUniqueID(uniqueID);

            boolean found = false;
            for (Iterator<String> it = room.iterator(); it.hasNext() && !found; ) {
                String userID = it.next();
                if (userID.equals(uniqueID)) {
                    found = true;
                    room.remove(userID);

                    // Create confirmation _after_ removing user from room
                    SocketMessage confirmation = confirmationBuilder
                        .withPayload(
                            payloadBuilder
                                .withVolunteerCount(sumVolunteersInRoom(roomID))
                                .build()
                        ).build();
                    for (String userIDToSendConfirmationMessageTo : room) {
                        ChatEndpoint.sockets
                            .get(userIDToSendConfirmationMessageTo)
                            .sendClient(ChatUtils.stringify(confirmation));
                    }
                    LOG.info("User has left the room");
                    LOG.info(uniqueID);
                }
            }

            if (room.size() <= 1) {
                if (room.size() == 1) {
                    // Send message that chat is closed to student
                    SocketMessage chatClosedMessage =
                        SocketMessage.Builder.newInstance()
                            .withMsgType(Msg.MessageEnum.CLOSE_CHAT)
                            .withPayload(
                                // Message can be basically empty. We only really want to
                                // send the MsgType
                                // CLOSE_CHAT
                                new LeaveMessage.Builder().build())
                            .build();
                    ChatEndpoint.sockets
                        .get(room.get(0))
                        .sendClient(ChatUtils.stringify(chatClosedMessage));
                }

                long chatDuration = TimeUnit.MILLISECONDS.toMinutes(
                    System.currentTimeMillis() - studentsEnteredChat.get(studentInfo.getUniqueID())
                );

                if (chatDuration > 4) {
                    mixpanelService.trackEventWithDuration(
                        MixpanelEvent.VOLUNTEER_FINISHED_HELP,
                        studentInfo,
                        chatDuration
                    );
                }

                studentsEnteredChat.remove(studentInfo.getUniqueID());
                rooms.remove(roomID);
                LOG.info("Room closed, all 'frivillige' has left");
            }
        } catch (Error e) {
            // Send pass errorMessage to the user who requested to leave chat
            SocketMessage errorMessage =
                SocketMessage.Builder.newInstance()
                    .withMsgType(Msg.MessageEnum.ERROR_LEAVING_CHAT)
                    .withPayload(new LeaveMessage.Builder().build())
                    .build();
            this.sendClient(ChatUtils.stringify(errorMessage));
            LOG.error("Error leaving room");
            LOG.error(e.getMessage());
        }
    }

    private void getAvailableForChat(String message) {
        RoomMessage payload = gson.fromJson(message, RoomMessage.class);

        List<Volunteer> volunteerNames =
            activeVolunteers.values().stream()
                .filter(x -> !x.getChatID().equals(this.id))
                .filter(x -> x.getRoomID() == null || !x.getRoomID().equals(payload.getRoomID()))
                .collect(Collectors.toList());

        SocketMessage list =
            SocketMessage.Builder.newInstance()
                .withMsgType(Msg.MessageEnum.AVAILABLE_CHAT)
                .withPayload(
                    new AvailableQueue.Builder().queueMembers(volunteerNames).build())
                .build();

        this.sendClient(ChatUtils.stringify(list));
    }

    void sendClient(String str) {
        LOG.info("Send following message to client: " + str);
        try {
            LOG.info("sendClient [isOpen={}]", this.session.isOpen());
            if (this.session.isOpen()) {
                this.session.getRemote().sendString(str);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void sendError(String err) {
        this.sendClient(String.format("{\"withMessage\": \"error\", \"error\": \"%s\"}", err));
    }

    private static SocketMessage getQueueMessage() {
        List<StudentInfo> studentInfoList = new ArrayList<>(ChatEndpoint.waitingRooms.values());

        return SocketMessage.Builder.newInstance()
            .withMsgType(Msg.MessageEnum.QUEUE_LIST)
            .withPayload(
                new QueueListMessage.Builder()
                    .queueMembers(studentInfoList)
                    .build())
            .build();
    }

    private void reconnectHandler(String msg) {
        Message reconnectMessage = gson.fromJson(msg, Message.class);
        String uniqueID = reconnectMessage.getUniqueID();
        List<String> roomIDs = new ArrayList<>();
        // Change uniqueID of this socket
        ChatEndpoint.sockets.remove(this.id); // Remove to set new id
        this.id = uniqueID;
        Optional<ChatEndpoint> reconnectedUser = Optional.ofNullable(
            ChatEndpoint.sockets.get(uniqueID));

        reconnectedUser.ifPresentOrElse(user ->
                ChatEndpoint.sockets.replace(uniqueID, this)
            , () -> ChatEndpoint.sockets.put(uniqueID, this));


        ClosedChat userFromReconnectedList = reconnectList.remove(uniqueID);
        if (userFromReconnectedList != null && userFromReconnectedList.isVolunteer()) {
            Volunteer temp = userFromReconnectedList.getVolunteer();
            this.volunteer = true;
            activeVolunteers.put(uniqueID, temp);
        }
        for (ConcurrentMap.Entry<String, List<String>> room : ChatEndpoint.rooms.entrySet()) {
            if (!room.getValue().contains(this.id)) {
                continue;
            }
            roomIDs.add(room.getKey());
        }

        ReconnectMessage payload =
            new ReconnectMessage.Builder().withUniqueID(this.id).withRoomIDs(roomIDs).build();
        SocketMessage socketMessage =
            SocketMessage.Builder.newInstance()
                .withMsgType(Msg.MessageEnum.RECONNECT)
                .withPayload(payload)
                .build();
        this.sendClient(ChatUtils.stringify(socketMessage));
        LOG.info("Client reconnected");
        LOG.info(uniqueID);
    }

    private static long sumVolunteersInRoom(String roomId) {
        return ChatEndpoint.rooms.get(roomId).stream()
            .filter(ChatEndpoint.activeVolunteers::containsKey)
            .count();
    }

    static void dispatchLeaveMessage(String message, String roomID) {
        List<String> room = ChatEndpoint.rooms.get(roomID);
        room.forEach(
            uid -> ChatEndpoint.sockets.get(uid).sendClient(
                ChatUtils.stringify(
                    SocketMessage.Builder.newInstance()
                        .withMsgType(Msg.MessageEnum.LEAVE_CHAT)
                        .withPayload(
                            new TextMessage.Builder()
                                .withUniqueID("NOTIFICATION")
                                .withMessage(message)
                                .withRoomID(roomID)
                                .withVolunteerCount(sumVolunteersInRoom(roomID))
                                .build())
                        .build()))
        );
    }

    public static void sendUpdateQueueMessageToVolunteers() {
        ChatEndpoint.activeVolunteers.keySet().forEach(key -> ChatEndpoint.sockets.get(key).sendClient(
            ChatUtils.stringify(getQueueMessage())
        ));
    }

    //TODO: Sandra, del opp i to funksjoner
    private void studentLeaveHandler(String msg) {
        LeaveMessage leaveMessage = gson.fromJson(msg, LeaveMessage.class);
        boolean isRemovedByVolunteer = "volunteer".equals(leaveMessage.getRemovedBy());

        try {
            if (isRemovedByVolunteer) {
                ChatEndpoint.updatePositionsInQueue();
                Message message = gson.fromJson(msg, Message.class);
                StudentInfo studentInfo = ChatEndpoint.waitingRooms.remove(message.getUniqueID());
                mixpanelService.trackEventWithStudentInformation(MixpanelEvent.VOLUNTEER_REMOVED_STUDENT_FROM_QUEUE, studentInfo);

            } else {
                StudentInfo studentInfo = ChatEndpoint.waitingRooms.remove(leaveMessage.getUniqueID());
                boolean leftRoom = false;

                for (ConcurrentMap.Entry<String, List<String>> room : ChatEndpoint.rooms.entrySet()) {
                    if (!room.getValue().contains(leaveMessage.getUniqueID())) {
                        continue;
                    }
                    leftRoom = true;
                    ChatEndpoint.rooms.get(room.getKey()).remove(leaveMessage.getUniqueID());
                    ChatEndpoint.dispatchLeaveMessage(
                        "Student har forlatt rommet",
                        room.getKey()
                    );
                }
                if (!leftRoom) {
                    ChatEndpoint.updatePositionsInQueue();
                    if (studentInfo != null) {
                        mixpanelService.trackEventWithStudentInformation(MixpanelEvent.STUDENT_LEFT_QUEUE, studentInfo);
                    }
                }
            }

            sendUpdateQueueMessageToVolunteers();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
