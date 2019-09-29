package no.capraconsulting.chatmessages;

public class LeaveMessage extends Message {

    protected String name;
    protected String roomID;
    protected String removedBy;
    private long volunteerCount;

    public String getName() {
        return name;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getRemovedBy() { return removedBy; }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private String name;
        private String roomID;
        private String removedBy;
        private long volunteerCount;

        public T withName(String name) {
            this.name = name;
            return self();
        }

        public T withRoomID(String roomID) {
            this.roomID = roomID;
            return self();
        }

        public T withRemovedBy(String removedBy) {
            this.removedBy = removedBy;
            return self();
        }

        public T withVolunteerCount(long volunteerCount) {
            this.volunteerCount = volunteerCount;
            return self();
        }

        public LeaveMessage build() {
            return new LeaveMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected LeaveMessage(Init<?> init) {
        super(init);
        this.name = init.name;
        this.roomID = init.roomID;
        this.removedBy = init.removedBy;
        this.volunteerCount = init.volunteerCount;
    }

}
