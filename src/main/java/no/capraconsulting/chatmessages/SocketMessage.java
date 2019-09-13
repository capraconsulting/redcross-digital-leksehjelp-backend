package no.capraconsulting.chatmessages;

import no.capraconsulting.enums.Msg;

import java.util.Date;

public class SocketMessage {

    //static class Builder();
    private Msg.MessageEnum msgType;
    private Message payload;

    public SocketMessage(Builder builder){
        msgType = builder.msgType;
        payload = builder.payload;
    }

    public Msg.MessageEnum getMsgType(){
        return msgType;
    }

    public Message getPayload(){
        return payload;
    }

    public static class Builder{

        private Msg.MessageEnum msgType;
        private Message payload;

        private Builder(){}

        public static Builder newInstance()
        {
            return new Builder();
        }

        public Builder withMsgType(Msg.MessageEnum msgType){
            this.msgType = msgType;
            return this;
        }

        public Builder withPayload(Message payload){
            this.payload = payload;
            return this;
        }

        public SocketMessage build(){
            return new SocketMessage(this);
        }
    }
}
