package no.capraconsulting.chat.messagehandler;

import com.google.gson.Gson;
import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.Rooms;
import no.capraconsulting.chat.state.Sockets;
import no.capraconsulting.chatmessages.Message;
import no.capraconsulting.chatmessages.TextMessage;
import no.capraconsulting.mixpanel.MixpanelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;

public abstract class MessageHandler<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    protected static final Gson GSON = new Gson();
    protected static final MixpanelService MIXPANEL_SERVICE = new MixpanelService();

    protected final ChatEndpoint currentSocket;

    public MessageHandler(ChatEndpoint currentSocket) {
        this.currentSocket = currentSocket;
    }

    protected abstract T convert(String payload);

    protected abstract void handle(T msg);

    public void handle(String payload) {
        handle(convert(payload));
    }

    protected static String generateID() {
        return UUID.randomUUID().toString();
    }

    public static void dispatchLeaveMessage(String message, String roomID) {
        Rooms.sendToAll(
            roomID,
            MessageType.LEAVE_CHAT,
            new TextMessage.Builder()
                .withUniqueID("NOTIFICATION")
                .withMessage(message)
                .withRoomID(roomID)
                .withVolunteerCount(Rooms.sumVolunteers(roomID))
                .build()
        );
    }

    public static void sendToSocket(String uid, MessageType messageType, Message payload) {
        Sockets.ifPresentOrElse(
            uid,
            socket -> socket.sendClient(messageType, payload),
            () -> LOG.error("Could not send message as socket with id {} was not present", uid)
        );
    }

    public static void sendToAll(Collection<String> sockets, MessageType messageType, Message payload) {
        for (String socket : sockets) {
            sendToSocket(socket, messageType, payload);
        }
    }
}
