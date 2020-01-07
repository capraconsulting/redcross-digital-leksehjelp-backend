package no.capraconsulting.chat;

import java.util.ArrayList;

public class StudentInfo {

    private String uniqueID;
    private String subject;
    private String grade;
    private String introText;
    private String nickname;
    private ArrayList<String> themes;
    private ChatType chatType;
    private int positionInQueue;
    private long timePlacedInQueue;

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void setPositionInQueue(int positionInQueue) {
        this.positionInQueue = positionInQueue;
    }

    public void decrementPositionInQueue() {
        this.positionInQueue--;
    }

    public ArrayList<String> getThemes() {
        return themes;
    }

    public void setThemes(ArrayList<String> themes) {
        this.themes = themes;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String userID) {
        this.uniqueID = userID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getNickname(){
        return nickname;
    }

    public void setIntroText(String introText){
        this.introText = introText;
    }

    public String getIntroText(){
        return introText;
    }

    public long getTimePlacedInQueue() {
        return timePlacedInQueue;
    }

    public void setTimePlacedInQueue(long timePlacedInQueue) {
        this.timePlacedInQueue = timePlacedInQueue;
    }
}
