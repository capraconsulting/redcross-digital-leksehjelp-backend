package no.capraconsulting.chatmessages;

import no.capraconsulting.chat.MessageType;

public class SocketMessage {
    private final MessageType msgType;
    private final Message payload;

    public SocketMessage(MessageType msgType, Message payload){
        this.msgType = msgType;
        this.payload = payload;
    }
}
