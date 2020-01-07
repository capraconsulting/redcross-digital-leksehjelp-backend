package no.capraconsulting.chat.state;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.messagehandler.MessageHandler;
import no.capraconsulting.chatmessages.ActiveSubjectsMessage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static no.capraconsulting.utils.EndpointUtils.getActiveSubjects;

public abstract class Sockets {
    // K: SocketID, V: Socket
    private static final ConcurrentMap<String, ChatEndpoint> SOCKETS = new ConcurrentHashMap<>();

    public static void reconnect(String newSocketId, ChatEndpoint socket) {
        SOCKETS.remove(socket.getId());
        socket.setId(newSocketId);
        SOCKETS.put(newSocketId, socket);
    }

    public static boolean contains(ChatEndpoint socket) {
        return SOCKETS.containsKey(socket.getId());
    }

    public static void remove(ChatEndpoint socket) {
        SOCKETS.remove(socket.getId());
    }

    public static void add(ChatEndpoint socket) {
        SOCKETS.put(socket.getId(), socket);
    }

    public static void sendActiveSubjectsToAll() {
        MessageHandler.sendToAll(
            SOCKETS.keySet(),
            MessageType.UPDATE_ACTIVE_SUBJECTS,
            new ActiveSubjectsMessage.Builder()
                .activeSubjects(getActiveSubjects())
                .build()
        );
    }

    public static void ifPresent(String socketId, Consumer<ChatEndpoint> action) {
        Optional.ofNullable(SOCKETS.get(socketId)).ifPresent(action);
    }

    public static void ifPresentOrElse(String socketId, Consumer<ChatEndpoint> action, Runnable emptyAction) {
        Optional.ofNullable(SOCKETS.get(socketId)).ifPresentOrElse(action, emptyAction);
    }
}
