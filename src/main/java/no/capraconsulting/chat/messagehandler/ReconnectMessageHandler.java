package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.ClosedChat;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.*;
import no.capraconsulting.chatmessages.Message;
import no.capraconsulting.chatmessages.ReconnectMessage;
import no.capraconsulting.chatmessages.Volunteer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ReconnectMessageHandler extends MessageHandler<Message> {
    private static Logger LOG = LoggerFactory.getLogger(ReconnectMessageHandler.class);

    public ReconnectMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected Message convert(String payload) {
        return GSON.fromJson(payload, Message.class);
    }

    @Override
    protected void handle(Message msg) {
        String uniqueID = msg.getUniqueID();

        Sockets.reconnect(uniqueID, currentSocket);

        ClosedChat userFromReconnectedList = ReconnectList.remove(uniqueID);

        if (userFromReconnectedList != null && userFromReconnectedList.isVolunteer()) {
            Volunteer temp = userFromReconnectedList.getVolunteer();
            ActiveVolunteers.add(uniqueID, temp);
        }

        List<String> roomIds = Rooms
            .getAllWithPerson(currentSocket.getId())
            .stream()
            .map(Room::getId)
            .collect(Collectors.toList());

        currentSocket.sendClient(
            MessageType.RECONNECT,
            new ReconnectMessage.Builder()
                .withUniqueID(currentSocket.getId())
                .withRoomIDs(roomIds)
                .build()
        );
        LOG.info("Client reconnected");
        LOG.info(uniqueID);
    }
}
