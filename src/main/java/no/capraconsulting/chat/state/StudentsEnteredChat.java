package no.capraconsulting.chat.state;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public abstract class StudentsEnteredChat {
    public static final ConcurrentMap<String, Long> STUDENTS_ENTERED_CHAT = new ConcurrentHashMap<>();

    public static void remove(String socketId) {
        STUDENTS_ENTERED_CHAT.remove(socketId);
    }

    public static void startTimer(String socketId) {
        STUDENTS_ENTERED_CHAT.put(socketId, System.currentTimeMillis());
    }

    public static long calculateDuration(String socketId) {
        return TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - STUDENTS_ENTERED_CHAT.remove(socketId)
        );
    }
}
