package no.capraconsulting.chatmessages;

import no.capraconsulting.chat.StudentInfo;
import java.util.List;

public class RoomMessage extends Message {

    private String studentID;
    private String volName;
    private String roomID;
    private StudentInfo studentInfo;
    private List<TextMessage> chatHistory;
    private String talkyID;
    protected String author;
    private Integer volunteerCount;

    public String getVolName() {
        return volName;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getRoomID() {
        return roomID;
    }

    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    public List<TextMessage> getChatHistory(){
        return chatHistory;
    }

    public String getTalkyID() {
        return talkyID;
    }

    public void setAuthor(String author) { this.author = author;}
    public String getAuthor(){ return author; }

    public Integer getVolunteerCount() {
        return volunteerCount;
    }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {

        private String studentID;
        private String volName;
        private String roomID;
        private String talkyID;
        private StudentInfo studentInfo;
        private List<TextMessage> chatHistory;
        private Integer volunteerCount;

        public T withStudentID(String studentID) {
            this.studentID = studentID;
            return self();
        }

        public T withVolName(String volName) {
            this.volName = volName;
            return self();
        }

        public T withRoomID(String roomID) {
            this.roomID = roomID;
            return self();
        }

        public T withTalkyID(String talkyID) {
            this.talkyID = talkyID;
            return self();
        }

        public T withStudentInfo(StudentInfo studentInfo){
            this.studentInfo = studentInfo;
            return self();
        }

        public T withChatHistory(List<TextMessage> chatHistory){
            this.chatHistory = chatHistory;
            return self();
        }

        public T withVolunteerCount(Integer volunteerCount) {
            this.volunteerCount = volunteerCount;
            return self();
        }

        public RoomMessage build() {
            return new RoomMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    RoomMessage(Init<?> init) {
        super(init);
        this.studentID = init.studentID;
        this.roomID = init.roomID;
        this.talkyID = init.talkyID;
        this.studentInfo = init.studentInfo;
        this.chatHistory = init.chatHistory;
        this.volName = init.volName;
        this.volunteerCount = init.volunteerCount;
    }
}
