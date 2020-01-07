package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.*;
import no.capraconsulting.chat.state.*;
import no.capraconsulting.chatmessages.RoomMessage;
import no.capraconsulting.enums.MixpanelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateRoomMessageHandler extends MessageHandler<RoomMessage> {
    private static Logger LOG = LoggerFactory.getLogger(GenerateRoomMessageHandler.class);

    public GenerateRoomMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected RoomMessage convert(String payload) {
        return GSON.fromJson(payload, RoomMessage.class);
    }

    @Override
    protected void handle(RoomMessage msg) {
        String studentID = msg.getStudentID();

        StudentInfo studentInfo = WaitingRoom.remove(studentID);

        StudentsEnteredChat.startTimer(studentInfo.getUniqueID());

        String roomID = Rooms.create(currentSocket.getId(), studentInfo.getUniqueID());

        if (ActiveVolunteers.contains(currentSocket)) {
            ActiveVolunteers.setRoomId(currentSocket.getId(), roomID);
        }

        RoomMessage.Builder payloadBuilder = new RoomMessage.Builder()
            .withUniqueID(msg.getUniqueID())
            .withRoomID(roomID)
            .withStudentID(studentID)
            .withVolName(msg.getVolName())
            .withVolunteerCount(1L);

        if (studentInfo.getChatType() == ChatType.LEKSEHJELP_VIDEO || studentInfo.getChatType() == ChatType.MESTRING_VIDEO) {
            // With talkyID
            String talkyID = generateID();
            payloadBuilder.withTalkyID(talkyID);
            LOG.info("Video Chat created, id: {}", talkyID);
        }

        MIXPANEL_SERVICE.trackEventWithStudentInformation(MixpanelEvent.VOLUNTEER_STARTED_HELP, studentInfo);

        Rooms.sendToAll(
            roomID,
            MessageType.DISTRIBUTE_ROOM,
            payloadBuilder.build()
        );

        // Update positions in queue
        Queues.remove(studentInfo.getSubject(), studentID);

        WaitingRoom.moveEveryoneForwardInQueue();

        ActiveVolunteers.sendQueueListToAll();
    }
}
