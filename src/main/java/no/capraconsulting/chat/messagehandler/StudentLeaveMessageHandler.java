package no.capraconsulting.chat.messagehandler;

import no.capraconsulting.chat.ChatEndpoint;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.chat.state.ActiveVolunteers;
import no.capraconsulting.chat.state.Room;
import no.capraconsulting.chat.state.Rooms;
import no.capraconsulting.chat.state.WaitingRoom;
import no.capraconsulting.chatmessages.LeaveMessage;
import no.capraconsulting.enums.MixpanelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class StudentLeaveMessageHandler extends MessageHandler<LeaveMessage> {
    private static Logger LOG = LoggerFactory.getLogger(StudentLeaveMessageHandler.class);

    public StudentLeaveMessageHandler(ChatEndpoint currentSocket) {
        super(currentSocket);
    }

    @Override
    protected LeaveMessage convert(String payload) {
        return GSON.fromJson(payload, LeaveMessage.class);
    }

    @Override
    protected void handle(LeaveMessage msg) {
        boolean isRemovedByVolunteer = "volunteer".equals(msg.getRemovedBy());

        // Nullable
        StudentInfo studentInfo = WaitingRoom.remove(msg.getUniqueID());

        if (isRemovedByVolunteer) {
            WaitingRoom.moveEveryoneForwardInQueue();
            MIXPANEL_SERVICE.trackEventWithStudentInformation(MixpanelEvent.VOLUNTEER_REMOVED_STUDENT_FROM_QUEUE, studentInfo);
        } else {
            List<Room> rooms = Rooms.getAllWithPerson(msg.getUniqueID());

            if (rooms.size() == 0) {
                // Student did not exist in any room --> left from waiting room
                WaitingRoom.moveEveryoneForwardInQueue();
                MIXPANEL_SERVICE.trackEventWithStudentInformation(MixpanelEvent.STUDENT_LEFT_QUEUE, studentInfo);
            } else {
                // Remove student from each room (s)he was in
                rooms.forEach(room -> {
                    Rooms.removePerson(room.getId(), msg.getUniqueID());

                    dispatchLeaveMessage("Student har forlatt rommet", room.getId());
                });
            }
        }

        ActiveVolunteers.sendQueueListToAll();
    }
}
