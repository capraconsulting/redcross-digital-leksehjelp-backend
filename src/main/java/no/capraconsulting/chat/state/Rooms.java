package no.capraconsulting.chat.state;

import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.messagehandler.MessageHandler;
import no.capraconsulting.chatmessages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Rooms {
    private static Logger LOG = LoggerFactory.getLogger(Rooms.class);

    // K: RoomID, V: SocketID[]
    private static final ConcurrentMap<String, List<String>> ROOMS = new ConcurrentHashMap<>();

    public static List<String> get(String roomId) {
        return ROOMS.get(roomId);
    }

    /**
     * Create a new room
     *
     * @param initialMembers - initial list of persons in the room
     * @return - id of the created room
     */
    public static String create(String... initialMembers) {
        String id = UUID.randomUUID().toString();
        ROOMS.put(id, new ArrayList<>(Arrays.asList(initialMembers)));
        LOG.info("New room created: {}", id);
        return id;
    }

    /**
     * Send a message to every person in a room
     *
     * @param roomId - id of room
     * @param messageType - type of message
     * @param payload - payload of the message
     */
    public static void sendToAll(String roomId, MessageType messageType, Message payload) {
        MessageHandler.sendToAll(ROOMS.get(roomId), messageType, payload);
    }

    /**
     * Count every volunteer in a room
     *
     * @param roomId - id of room to perform count of
     * @return count of volunteers in room
     */
    public static long sumVolunteers(String roomId) {
        return ROOMS.get(roomId).stream()
            .filter(ActiveVolunteers::isActive)
            .count();
    }

    /**
     * Close a chat room
     *
     * @param roomId - id of room to close
     */
    public static void close(String roomId) {
        ROOMS.remove(roomId);
        LOG.info("Room closed: {}", roomId);
    }

    /**
     * Remove person from room
     *
     * @param roomId - id of room
     * @param socketId - id of person to remove
     */
    public static void removePerson(String roomId, String socketId) {
        List<String> room = ROOMS.get(roomId);

        if (room == null) {
            LOG.warn("Room did not exist: {}", roomId);
        } else {
            room.remove(socketId);
            LOG.info("Person removed from room: [person={}, room={}]", socketId, roomId);
        }
    }

    /**
     * Get all rooms containing a person
     *
     * @return a stream of all rooms in a simple format containing the id and all members of the room
     */
    public static List<Room> getAllWithPerson(String socketId) {
        return ROOMS.entrySet().stream()
            .filter(e -> e.getValue().contains(socketId))
            .map(e -> new Room(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
