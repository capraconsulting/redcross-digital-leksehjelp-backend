package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.WaitingRoom;
import no.capraconsulting.chatmessages.QueueListMessage;

public class QueueListMessageHandler extends MessageHandler<Object> {
    public QueueListMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected Object convert(String payload) {
        return payload;
    }

    @Override
    protected void handle(Object msg) {
        currentSocket.sendClient(
            MessageType.QUEUE_LIST,
            new QueueListMessage.Builder()
                .queueMembers(WaitingRoom.getAll())
                .build()
        );
    }
}
