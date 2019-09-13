package no.capraconsulting.chatmessages;

import org.w3c.dom.Text;
import no.capraconsulting.globalclasses.File;

import java.util.Date;
import java.util.ArrayList;

public class TextMessage extends Message{

    private String message;
    private ArrayList<File> files;
    private String imgUrl;
    private String author;
    private String roomID;

    public String getMessage() {
        return message;
    }

    public String getImgUrl() { return imgUrl; }

    public String getAuthor() { return author; }

    public String getRoomID(){ return roomID; }

    public ArrayList<File> getFiles() {
        return files;
    }

    protected static abstract class Init<T extends TextMessage.Init<T>> extends Message.Init<T> {

        private String message;
        private ArrayList<File> files;
        private String imgUrl;
        private String author;
        private String roomID;

        public T withMessage(String message) {
            this.message = message;
            return self();
        }

        public T withRoomID(String roomID){
            this.roomID = roomID;
            return self();
        }

        public T withAuthor(String author){
            this.author = author;
            return self();

        }
        public T withFiles(ArrayList<File> files){
            this.files = files;
            return self();
        }

        public T withImgUrl(String imgUrl){
            this.imgUrl = imgUrl;
            return self();
        }
        public TextMessage build() {
            return new TextMessage(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected TextMessage(Init<?> init) {
        super(init);
        this.author = init.author;
        this.message = init.message;
        this.files = init.files;
        this.imgUrl = init.imgUrl;
        this.roomID = init.roomID;
    }
}
