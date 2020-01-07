package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.Sockets;
import no.capraconsulting.chatmessages.Volunteer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVolunteerMessageHandler extends MessageHandler<Volunteer> {
    private static Logger LOG = LoggerFactory.getLogger(SetVolunteerMessageHandler.class);

    public SetVolunteerMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected Volunteer convert(String payload) {
        return GSON.fromJson(payload, Volunteer.class);
    }

    @Override
    protected void handle(Volunteer msg) {
        msg.setChatID(currentSocket.getId());
        ActiveVolunteers.add(msg.getChatID(), msg);
        LOG.info("The number of active volunteers is :{}", ActiveVolunteers.count());

        Sockets.sendActiveSubjectsToAll();
    }
}
