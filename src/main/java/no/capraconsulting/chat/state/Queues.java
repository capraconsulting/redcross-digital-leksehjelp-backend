package no.capraconsulting.chat.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Queues {
    private static Logger LOG = LoggerFactory.getLogger(Queues.class);

    // K: Subject, V: SocketID[]
    private static final ConcurrentMap<String, List<String>> QUEUES = new ConcurrentHashMap<>();

    public static void add(String subject, String studentId) {
        QUEUES.putIfAbsent(subject, new ArrayList<>());
        QUEUES.get(subject).add(studentId);
    }

    public static void remove(String subject, String studentId) {
        List<String> queue = QUEUES.get(subject);

        if (queue == null) {
            LOG.warn("Queue did not exist: {}", subject);
        } else {
            queue.remove(studentId);
        }
    }
}
