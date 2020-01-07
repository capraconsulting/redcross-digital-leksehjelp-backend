package no.capraconsulting.chat.state;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.chat.messagehandler.MessageHandler;
import no.capraconsulting.chatmessages.StudentInfoMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public abstract class WaitingRoom {
    // K: SocketID, V: StudentInfo
    private static final ConcurrentMap<String, StudentInfo> WAITING_ROOM = new ConcurrentHashMap<>();

    public static StudentInfo remove(String socketId) {
        return WAITING_ROOM.remove(socketId);
    }

    public static boolean contains(ChatEndpoint chatEndpoint) {
        return WAITING_ROOM.containsKey(chatEndpoint.getId());
    }

    public static void add(ChatEndpoint chatEndpoint, StudentInfo studentInfo) {
        WAITING_ROOM.put(chatEndpoint.getId(), studentInfo);
    }

    public static List<StudentInfo> getAll() {
        return new ArrayList<>(WAITING_ROOM.values());
    }

    public static void ifPresentOrElse(ChatEndpoint socket, Consumer<StudentInfo> action, Runnable emptyAction) {
        Optional.ofNullable(WAITING_ROOM.get(socket.getId())).ifPresentOrElse(action, emptyAction);
    }

    public static void moveEveryoneForwardInQueue() {
        WAITING_ROOM.forEach((uid, si) -> {
            si.decrementPositionInQueue();

            MessageHandler.sendToSocket(
                uid,
                MessageType.UPDATE_QUEUE,
                new StudentInfoMessage.Builder().withStudentInfo(si).build()
            );
        });
    }

    public static int size() {
        return WAITING_ROOM.size();
    }
}
