package no.capraconsulting.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;

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
                    for (Iterator<ClosedChat> it =
                         ChatEndpoint.reconnectList.values().iterator();
                         it.hasNext(); ) {
                        ClosedChat first = it.next();
                        if (first.isTimesUp()) {
                            ChatEndpoint.reconnectList.values().remove(first);

                            StudentInfo removedFromWaitingRoom =
                                ChatEndpoint.waitingRooms.remove(first.getChat().getId());
                            if (removedFromWaitingRoom != null) {
                                ChatEndpoint.updatePositionsInQueue();
                                queueUpdated = true;
                            }

                            for (ConcurrentMap.Entry<String, List<String>> room :
                                ChatEndpoint.rooms.entrySet()) {
                                if (!room.getValue().contains(first.getChat().getId())) {
                                    continue;
                                }
                                ChatEndpoint.rooms
                                    .get(room.getKey())
                                    .remove(first.getChat().getId());
                                if (first.isVolunteer()) {
                                    ChatEndpoint.dispatchLeaveMessage(
                                        String.format("%s har forlatt Chatten", first.getVolunteer().getName()),
                                        room.getKey(),
                                        false
                                    );
                                } else {
                                    ChatEndpoint.dispatchLeaveMessage(
                                        "Student har forlatt Chatten",
                                        room.getKey(),
                                        true
                                    );
                                }
                            }
                            LOG.info("Fjernet fra liste");
                        }
                    }

                    if (queueUpdated) {
                        ChatEndpoint.sendUpdateQueueMessageToVolunteers();
                    }
                }
            },
            delay,
            period);
    }
}
