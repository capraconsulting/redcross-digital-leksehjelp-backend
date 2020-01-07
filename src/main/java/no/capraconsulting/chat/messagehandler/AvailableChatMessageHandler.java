package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chatmessages.AvailableQueue;
import no.capraconsulting.chatmessages.RoomMessage;
import no.capraconsulting.chatmessages.Volunteer;

import java.util.List;
import java.util.stream.Collectors;

public class AvailableChatMessageHandler extends MessageHandler<RoomMessage> {
    public AvailableChatMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected RoomMessage convert(String payload) {
        return GSON.fromJson(payload, RoomMessage.class);
    }

    @Override
    protected void handle(RoomMessage payload) {
        List<Volunteer> volunteerNames =
            ActiveVolunteers.stream()
                .filter(x -> !x.getChatID().equals(currentSocket.getId()))
                .filter(x -> x.getRoomID() == null || !x.getRoomID().equals(payload.getRoomID()))
                .collect(Collectors.toList());

        currentSocket.sendClient(
            MessageType.AVAILABLE_CHAT,
            new AvailableQueue.Builder().queueMembers(volunteerNames).build()
        );
    }
}
