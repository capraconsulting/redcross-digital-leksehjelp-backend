package no.capraconsulting.chat;

import no.capraconsulting.chatmessages.Volunteer;

import java.util.Timer;
import java.util.TimerTask;

public class ClosedChat extends Thread {

    private boolean timesUp = false;
    private int interval = 15;
    private Timer timer;
    private ChatEndpoint chat;
    private Volunteer volunteer = null;
    private boolean isVolunteer = false;

    ClosedChat(ChatEndpoint chat) {
        this.chat = chat;
    }

    boolean isTimesUp() {
        return timesUp;
    }

    ChatEndpoint getChat() {
        return chat;
    }

    boolean isVolunteer() { return isVolunteer; }

    Volunteer getVolunteer(){ return volunteer; }

    void setVolunteer(Volunteer volunteer){
        this.volunteer = volunteer;
        isVolunteer = true;
    }

    @Override
    public void run() {
        int delay = 1000;
        int period = 1000;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                setInterval();
            }
        }, delay, period);
    }

    private void setInterval() {
        interval--;
        if (interval == 0) {
            timesUp = true;
            timer.cancel();
        }
    }
}
