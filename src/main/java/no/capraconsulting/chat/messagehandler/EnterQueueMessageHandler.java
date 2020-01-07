package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.MessageType;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.Queues;
import no.capraconsulting.chat.state.WaitingRoom;
import no.capraconsulting.chatmessages.StudentInfoMessage;
import no.capraconsulting.config.PropertiesHelper;
import no.capraconsulting.enums.MixpanelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class EnterQueueMessageHandler extends MessageHandler<StudentInfo> {
    private static Logger LOG = LoggerFactory.getLogger(EnterQueueMessageHandler.class);

    private static AtomicInteger studentNicknameCounter = new AtomicInteger(1);

    private static int studentNicknameCounterMaxValue = PropertiesHelper.getIntProperty(
        PropertiesHelper.getProperties(),
        PropertiesHelper.NICKNAME_COUNTER_MAX,
        Integer.MAX_VALUE
    );

    public EnterQueueMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected StudentInfo convert(String payload) {
        return GSON.fromJson(payload, StudentInfo.class);
    }

    @Override
    protected void handle(StudentInfo studentInfo) {
        studentInfo.setUniqueID(currentSocket.getId());

        int nicknameCounter = studentNicknameCounter.getAndIncrement();
        if (nicknameCounter >= studentNicknameCounterMaxValue) {
            studentNicknameCounter.set(1);
        }
        studentInfo.setNickname(String.format("Elev #%s", nicknameCounter));

        // Add to hashMap to display queue positions
        Queues.add(studentInfo.getSubject(), currentSocket.getId());

        // +1 because this student is not currently in the waiting room
        studentInfo.setPositionInQueue(WaitingRoom.size() + 1);

        if (!WaitingRoom.contains(currentSocket)) {
            studentInfo.setTimePlacedInQueue(System.currentTimeMillis());

            WaitingRoom.add(currentSocket, studentInfo);

            currentSocket.sendClient(
                MessageType.CONFIRMED_QUEUE,
                new StudentInfoMessage.Builder().withStudentInfo(studentInfo).build()
            );

            MIXPANEL_SERVICE.trackEventWithStudentInformation(MixpanelEvent.STUDENT_ENTERED_QUEUE, studentInfo);
            LOG.info("New student put in queue, queue length: {}", WaitingRoom.size());

            ActiveVolunteers.sendQueueListToAll();
        }
    }
}
