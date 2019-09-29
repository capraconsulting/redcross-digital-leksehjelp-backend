package no.capraconsulting.chatmessages;

import java.util.List;

public class ActiveSubjectsMessage extends Message {
    private List<String> activeSubjects;

    private List<String> getActiveSubjects() { return this.activeSubjects; }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private List<String> activeSubjects;

        public T activeSubjects(List<String> activeSubjects) {
            this.activeSubjects = activeSubjects;
            return self();
        }

        public ActiveSubjectsMessage build() {
            return new ActiveSubjectsMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    public ActiveSubjectsMessage(Init<?> init) {
        super(init);
        this.activeSubjects = init.activeSubjects;
    }
}
