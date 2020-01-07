package no.capraconsulting.chat;

import no.capraconsulting.chat.messagehandler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public enum MessageType {
    CONNECTION,
    CONFIRMED_QUEUE,
    DISTRIBUTE_ROOM,
    ERROR_LEAVING_CHAT,
    CLOSE_CHAT,
    PING,
    UPDATE_ACTIVE_SUBJECTS,

    ENTER_QUEUE(EnterQueueMessageHandler::new),
    UPDATE_QUEUE(UpdateQueueMessageHandler::new),
    GENERATE_ROOM(GenerateRoomMessageHandler::new),
    QUEUE_LIST(QueueListMessageHandler::new),
    TEXT(TextMessageHandler::new),
    JOIN_CHAT(JoinChatMessageHandler::new),
    LEAVE_CHAT(LeaveChatMessageHandler::new),
    AVAILABLE_CHAT(AvailableChatMessageHandler::new),
    RECONNECT(ReconnectMessageHandler::new),
    SET_VOLUNTEER(SetVolunteerMessageHandler::new),
    STUDENT_LEAVE(StudentLeaveMessageHandler::new),
    REMOVE_STUDENT_FROM_QUEUE(StudentLeaveMessageHandler::new);

    private static Logger LOG = LoggerFactory.getLogger(MessageType.class);

    MessageType() {
    }

    private Function<ChatEndpoint, MessageHandler<?>> handlerConstructor;

    MessageType(Function<ChatEndpoint, MessageHandler<?>> handlerConstructor) {
        this.handlerConstructor = handlerConstructor;
    }

    public void handle(ChatEndpoint currentSocket, String payload) {
        if (handlerConstructor != null) {
            MessageHandler<?> messageHandler = handlerConstructor.apply(currentSocket);
            messageHandler.handle(payload);
        } else if (this == PING) {
            LOG.info("pong");
        } else {
            LOG.info("No handler configured for messages of type {}", this);
        }
    }
}
