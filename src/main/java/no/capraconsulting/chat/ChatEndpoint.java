package no.capraconsulting.chat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.ReconnectList;
import no.capraconsulting.chat.state.Sockets;
import no.capraconsulting.chatmessages.ActiveSubjectsMessage;
import no.capraconsulting.chatmessages.Message;
import no.capraconsulting.chatmessages.SocketMessage;
import no.capraconsulting.chatmessages.Volunteer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static no.capraconsulting.utils.EndpointUtils.getActiveSubjects;


public class ChatEndpoint extends WebSocketAdapter {
    private static Logger LOG = LoggerFactory.getLogger(ChatEndpoint.class);

    private static final Gson gson = new Gson();

    private Session session;
    private String id;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    private String generateID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onWebSocketText(String message) {
        LOG.info("Retrieved socket message with message: " + message);
        JsonParser parser = new JsonParser();
        JsonObject jsonMsg = (JsonObject) parser.parse(message);
        String payload = jsonMsg.get("payload").toString();
        JsonElement msgType = jsonMsg.get("msgType");
        MessageType messageType = gson.fromJson(msgType.toString(), MessageType.class);

        LOG.info(messageType.name());

        messageType.handle(this, payload);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        LOG.info("onWebSocketClose() called, status code: " + statusCode + ", reason: " + reason);

        if (Sockets.contains(this)) {
            // remove connection
            LOG.info("Disconnecting socket\n{}", this.id);
            ClosedChat closedChat = new ClosedChat(this);

            if (ActiveVolunteers.contains(this)) {
                Volunteer volunteer = ActiveVolunteers.remove(this.id);
                closedChat.setVolunteer(volunteer);

                Sockets.sendActiveSubjectsToAll();
            }

            closedChat.run();

            ReconnectList.add(this, closedChat);
            Sockets.remove(this);
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        this.id = generateID();

        Sockets.add(this);

        ActiveSubjectsMessage payload = new ActiveSubjectsMessage.Builder()
            .withUniqueID(this.id)
            .activeSubjects(getActiveSubjects())
            .build();

        this.sendClient(MessageType.CONNECTION, payload);
        LOG.info("New client connected\n{}", payload);
    }


    public void sendClient(MessageType messageType, Message payload) {
        String str = gson.toJson(new SocketMessage(messageType, payload));

        LOG.info("Send following message to client: {}", str);
        try {
            LOG.info("sendClient [isOpen={}]", this.session.isOpen());
            if (this.session.isOpen()) {
                this.session.getRemote().sendString(str);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
