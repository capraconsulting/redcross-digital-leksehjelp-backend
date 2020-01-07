package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.Rooms;
import no.capraconsulting.chat.state.Sockets;
import no.capraconsulting.chatmessages.RoomMessage;
import no.capraconsulting.chatmessages.TextMessage;

import java.util.List;

public class JoinChatMessageHandler extends MessageHandler<RoomMessage> {
    public JoinChatMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected RoomMessage convert(String payload) {
        return GSON.fromJson(payload, RoomMessage.class);
    }

    @Override
    protected void handle(RoomMessage payload) {
        String roomID = payload.getRoomID();
        String talkyID = payload.getTalkyID();
        String uniqueID = payload.getUniqueID();

        List<String> room = Rooms.get(roomID);

        Sockets.ifPresent(uniqueID, socket -> room.add(uniqueID));


        // Update receiver's roomId
        ActiveVolunteers.setRoomId(uniqueID, roomID);

        // Update sender's roomId
        ActiveVolunteers.setRoomId(currentSocket.getId(), roomID);

        sendToSocket(
            uniqueID,
            MessageType.JOIN_CHAT,
            new RoomMessage.Builder()
                .withUniqueID(uniqueID)
                .withRoomID(roomID)
                .withTalkyID(talkyID)
                .withStudentInfo(payload.getStudentInfo())
                .withChatHistory(payload.getChatHistory())
                .withVolunteerCount(Rooms.sumVolunteers(roomID))
                .build()
        );

        Rooms.sendToAll(
            roomID,
            MessageType.TEXT,
            new TextMessage.Builder()
                .withUniqueID("NOTIFICATION")
                .withRoomID(roomID)
                .withMessage(String.format("%s har blitt med i chatten.", payload.getVolName()))
                .build()
        );
    }
}
