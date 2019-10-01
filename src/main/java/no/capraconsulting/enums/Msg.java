package no.capraconsulting.enums;

public interface Msg {

    enum MessageEnum {
        CONNECTION,
        ENTER_QUEUE,
        UPDATE_QUEUE,
        CONFIRMED_QUEUE,
        DISTRIBUTE_ROOM,
        GENERATE_ROOM,
        QUEUE_LIST,
        TEXT,
        JOIN_CHAT,
        LEAVE_CHAT,
        AVAILABLE_CHAT,
        ERROR_LEAVING_CHAT,
        CLOSE_CHAT,
        PING,
        RECONNECT,
        SET_VOLUNTEER,
        STUDENT_LEAVE,
        REMOVE_STUDENT_FROM_QUEUE,
        UPDATE_ACTIVE_SUBJECTS,
    }
}
