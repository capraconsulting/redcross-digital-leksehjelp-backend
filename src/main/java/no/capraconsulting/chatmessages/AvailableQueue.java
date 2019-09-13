package no.capraconsulting.chatmessages;

import java.util.List;

public class AvailableQueue extends Message{
    private List<Volunteer> queueMembers;

    private List<Volunteer> getQueueMembers() {
        return this.queueMembers;
    }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private List<Volunteer> queueMembers;

        public T queueMembers(List<Volunteer> queueMembers) {
            this.queueMembers = queueMembers;
            return self();
        }

        public AvailableQueue build() {
            return new AvailableQueue(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected AvailableQueue(Init<?> init) {
        super(init);
        this.queueMembers = init.queueMembers;
    }
}
