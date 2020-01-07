package no.capraconsulting.chat;

import no.capraconsulting.chat.messagehandler.MessageHandler;
import no.capraconsulting.chat.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RemoveEndedChats extends Thread {

    boolean allClear = false;
    Timer timer;
    private static Logger LOG = LoggerFactory.getLogger(RemoveEndedChats.class);

    @Override
    public void run() {
        int delay = 5000;
        int period = 5000;
        timer = new Timer();

        timer.scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    boolean queueUpdated = false;

                    for (Map.Entry<String, ClosedChat> entry : ReconnectList.getAll()) {
                        ClosedChat closedChat = entry.getValue();

                        if (closedChat.isTimesUp()) {
                            ReconnectList.remove(entry.getKey());

                            String chatId = closedChat.getChat().getId();

                            StudentInfo removedFromWaitingRoom = WaitingRoom.remove(chatId);

                            if (removedFromWaitingRoom != null) {
                                WaitingRoom.moveEveryoneForwardInQueue();
                                queueUpdated = true;
                            }

                            Rooms
                                .getAllWithPerson(chatId)
                                .forEach(room -> {
                                    Rooms.removePerson(room.getId(), chatId);

                                    String message = closedChat.isVolunteer()
                                        ? String.format("%s har forlatt Chatten", closedChat.getVolunteer().getName())
                                        : "Student har forlatt Chatten";

                                    MessageHandler.dispatchLeaveMessage(message, room.getId());
                                });

                            LOG.info("Fjernet fra liste");
                        }
                    }

                    if (queueUpdated) {
                        ActiveVolunteers.sendQueueListToAll();
                    }
                }
            },
            delay,
            period);
    }
}
