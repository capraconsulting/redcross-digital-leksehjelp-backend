package no.capraconsulting.chat.state;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.messagehandler.MessageHandler;
import no.capraconsulting.chatmessages.QueueListMessage;
import no.capraconsulting.chatmessages.Volunteer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class ActiveVolunteers {
    private static Logger LOG = LoggerFactory.getLogger(ActiveVolunteers.class);

    // K: SocketID, V: Volunteer
    private static final ConcurrentMap<String, Volunteer> ACTIVE_VOLUNTEERS = new ConcurrentHashMap<>();

    public static boolean isActive(String socketId) {
        return ACTIVE_VOLUNTEERS.containsKey(socketId);
    }

    public static int count() {
        return ACTIVE_VOLUNTEERS.size();
    }

    public static Volunteer get(String socketId) {
        return ACTIVE_VOLUNTEERS.get(socketId);
    }

    public static Volunteer remove(String socketId) {
        return ACTIVE_VOLUNTEERS.remove(socketId);
    }

    public static boolean contains(ChatEndpoint socket) {
        return ACTIVE_VOLUNTEERS.containsKey(socket.getId());
    }

    public static boolean isEmpty() {
        return ACTIVE_VOLUNTEERS.isEmpty();
    }

    public static Stream<Volunteer> stream() {
        return ACTIVE_VOLUNTEERS.values().stream();
    }

    public static void add(String socketId, Volunteer volunteer) {
        ACTIVE_VOLUNTEERS.put(socketId, volunteer);
    }

    public static void setRoomId(String volunteerId, String roomId) {
        Volunteer volunteer = ACTIVE_VOLUNTEERS.get(volunteerId);

        if (volunteer == null) {
            LOG.warn("Volunteer did not exist in active volunteers: {}", volunteerId);
        } else {
            volunteer.setRoomID(roomId);
        }
    }

    public static void sendQueueListToAll() {
        MessageHandler.sendToAll(
            ACTIVE_VOLUNTEERS.keySet(),
            MessageType.QUEUE_LIST,
            new QueueListMessage.Builder()
                .queueMembers(WaitingRoom.getAll())
                .build()
        );
    }
}
