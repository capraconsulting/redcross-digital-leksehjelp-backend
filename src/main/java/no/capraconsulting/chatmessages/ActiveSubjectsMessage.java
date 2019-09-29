package no.capraconsulting.chatmessages;

import java.util.Set;

public class ActiveSubjectsMessage extends Message {
    private Set<String> activeSubjects;

    private Set<String> getActiveSubjects() { return this.activeSubjects; }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private Set<String> activeSubjects;

        public T activeSubjects(Set<String> activeSubjects) {
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
