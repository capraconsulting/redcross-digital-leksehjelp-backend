package no.capraconsulting.chat.state;

import java.util.List;

public class Room {
    private final String id;
    private final List<String> members;

    public Room(String id, List<String> members) {
        this.id = id;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public List<String> getMembers() {
        return members;
    }

    public boolean notContainsPerson(String socketId) {
        return !members.contains(socketId);
    }
}
