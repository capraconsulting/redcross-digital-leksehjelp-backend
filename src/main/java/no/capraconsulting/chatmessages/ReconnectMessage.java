package no.capraconsulting.chatmessages;

import java.util.List;

public class ReconnectMessage extends Message {
    
    private List<String> roomIDs;


    public List<String> getRoomIDs() {
        return roomIDs;
    }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private List<String> roomIDs;

        public T withRoomIDs(List<String> roomIDs) {
            this.roomIDs = roomIDs;
            return self();
        }

        public ReconnectMessage build() {
            return new ReconnectMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    ReconnectMessage(Init<?> init) {
        super(init);
        this.roomIDs = init.roomIDs;
    }
}
