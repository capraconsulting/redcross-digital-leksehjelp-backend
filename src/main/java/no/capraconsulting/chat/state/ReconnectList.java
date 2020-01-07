package no.capraconsulting.chat.state;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.ClosedChat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ReconnectList {
    // K: SocketID, V: ClosedChat
    private static final ConcurrentMap<String, ClosedChat> RECONNECT_LIST = new ConcurrentHashMap<>();

    public static void add(ChatEndpoint socket, ClosedChat closedChat) {
        RECONNECT_LIST.put(socket.getId(), closedChat);
    }

    public static ClosedChat remove(String socketId) {
        return RECONNECT_LIST.remove(socketId);
    }

    public static Set<Map.Entry<String, ClosedChat>> getAll() {
        return RECONNECT_LIST.entrySet();
    }

    public static ClosedChat get(String socketId) {
        return RECONNECT_LIST.get(socketId);
    }
}
