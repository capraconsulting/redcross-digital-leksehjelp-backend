package no.capraconsulting.chatmessages;

import no.capraconsulting.chat.StudentInfo;

public class StudentInfoMessage extends Message {
    private StudentInfo info;

    public StudentInfo getInfo() {
        return info;
    }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {
        protected StudentInfo info;

        public T withStudentInfo(StudentInfo info) {
            this.info = info;
            return self();
        }

        public StudentInfoMessage build() {
            return new StudentInfoMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    private StudentInfoMessage(Init<?> init) {
        super(init);
        this.info = init.info;
    }
}
