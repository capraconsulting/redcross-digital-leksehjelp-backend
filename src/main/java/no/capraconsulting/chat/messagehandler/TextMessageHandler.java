package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.Rooms;
import no.capraconsulting.chatmessages.TextMessage;

import java.util.List;

public class TextMessageHandler extends MessageHandler<TextMessage> {
    public TextMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected TextMessage convert(String payload) {
        return GSON.fromJson(payload, TextMessage.class);
    }

    @Override
    public void handle(TextMessage msg) {
        List<String> room = Rooms.get(msg.getRoomID());

        if (!room.contains(msg.getUniqueID())) {
            // User that sent the message is not in the room
            return;
        }

        for (String socketID : room) {
            if (socketID.equals(currentSocket.getId())) {
                // skip me
                continue;
            }

            sendToSocket(socketID, MessageType.TEXT, msg);
        }
    }
}
