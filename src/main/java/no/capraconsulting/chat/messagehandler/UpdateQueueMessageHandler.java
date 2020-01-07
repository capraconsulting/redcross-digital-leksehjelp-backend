package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.chat.state.WaitingRoom;
import no.capraconsulting.chatmessages.StudentInfoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateQueueMessageHandler extends MessageHandler<StudentInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdateQueueMessageHandler.class);

    public UpdateQueueMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected StudentInfo convert(String payload) {
        return GSON.fromJson(payload, StudentInfo.class);
    }

    @Override
    protected void handle(StudentInfo msg) {
        WaitingRoom.ifPresentOrElse(
            currentSocket,
            studentInfo -> {
                studentInfo.setIntroText(msg.getIntroText());
                studentInfo.setGrade(msg.getGrade());
                studentInfo.setThemes(msg.getThemes());

                currentSocket.sendClient(
                    MessageType.UPDATE_QUEUE,
                    new StudentInfoMessage.Builder()
                        .withStudentInfo(studentInfo)
                        .build()
                );

                LOG.info("Student info updated\n{}", currentSocket.getId());
            },
            () -> LOG.error("Student is not already in queue")
        );
    }
}
