package no.capraconsulting.chatmessages;


public class Message {

    protected String uniqueID;

    public String getUniqueID() {
        return uniqueID;
    }


    protected Message(Init<?> init) {
        this.uniqueID = init.uniqueID;
    }

    protected static abstract class Init<T extends Init<T>> {

        private String uniqueID;

        protected abstract T self();

        public T withUniqueID(String uniqueID) {
            this.uniqueID = uniqueID;
            return self();
        }

        public Message build() {
            return new Message(this);
        }

    }

    public static class Builder extends Init<Builder> {

        @Override
        protected Builder self() {
            return this;
        }
    }
}
