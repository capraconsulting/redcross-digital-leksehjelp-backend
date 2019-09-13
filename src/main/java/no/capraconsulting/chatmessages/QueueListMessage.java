package no.capraconsulting.chatmessages;

import no.capraconsulting.chat.StudentInfo;

import java.util.List;

public class QueueListMessage extends Message {

    private List<StudentInfo> queueMembers;

    private List<StudentInfo> getQueueMembers() {
        return this.queueMembers;
    }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private List<StudentInfo> queueMembers;

        public T queueMembers(List<StudentInfo> queueMembers) {
            this.queueMembers = queueMembers;
            return self();
        }

        public QueueListMessage build() {
            return new QueueListMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected QueueListMessage(Init<?> init) {
        super(init);
        this.queueMembers = init.queueMembers;
    }

}
