package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.Rooms;
import no.capraconsulting.chat.state.StudentsEnteredChat;
import no.capraconsulting.chatmessages.LeaveMessage;
import no.capraconsulting.chatmessages.Message;
import no.capraconsulting.chatmessages.RoomMessage;
import no.capraconsulting.chatmessages.Volunteer;
import no.capraconsulting.enums.MixpanelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LeaveChatMessageHandler extends MessageHandler<RoomMessage> {
    private static Logger LOG = LoggerFactory.getLogger(LeaveChatMessageHandler.class);

    public LeaveChatMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected RoomMessage convert(String payload) {
        return GSON.fromJson(payload, RoomMessage.class);
    }

    @Override
    protected void handle(RoomMessage payload) {
        String roomID = payload.getRoomID();
        String uniqueID = payload.getUniqueID();

        Volunteer volunteer = ActiveVolunteers.get(uniqueID);
        StudentInfo studentInfo = payload.getStudentInfo();

        try {
            List<String> room = Rooms.get(roomID);

            String idToRemove = null;

            for (String userId : room) {
                if (userId.equals(uniqueID)) {
                    idToRemove = userId;
                    break;
                }
            }

            if (idToRemove != null) {
                room.remove(idToRemove);

                sendToAll(
                    room,
                    MessageType.LEAVE_CHAT,
                    new LeaveMessage.Builder()
                        .withName(volunteer.getName())
                        .withRoomID(roomID)
                        .withUniqueID(uniqueID)
                        .withVolunteerCount(Rooms.sumVolunteers(roomID))
                        .build()
                );

                LOG.info("User with id {} has left the room", uniqueID);
            }

            if (room.size() <= 1) {
                if (room.size() == 1) {
                    // Send message that chat is closed to student
                    sendToSocket(
                        room.get(0),
                        MessageType.CLOSE_CHAT,
                        new LeaveMessage.Builder().build()
                    );
                }

                long chatDuration = StudentsEnteredChat.calculateDuration(studentInfo.getUniqueID());

                if (chatDuration > 4) {
                    MIXPANEL_SERVICE.trackEventWithDuration(
                        MixpanelEvent.VOLUNTEER_FINISHED_HELP,
                        studentInfo,
                        chatDuration
                    );
                }

                Rooms.close(roomID);
            }
        } catch (Error e) {
            // Send pass errorMessage to the user who requested to leave chat
            currentSocket.sendClient(MessageType.ERROR_LEAVING_CHAT, new LeaveMessage.Builder().build());
            LOG.error("Error leaving room");
            LOG.error(e.getMessage());
        }
    }
}
