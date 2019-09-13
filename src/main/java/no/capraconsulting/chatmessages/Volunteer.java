package no.capraconsulting.chatmessages;

public class Volunteer extends Message {
    private String id;
    private String name;
    private String bioText;
    private String email;
    private String imgUrl;
    private String chatID;
    private String roomID;

    public String getId() {
        return id;
    }
    public String getName(){return name;}
    public String getBioText(){ return bioText;}
    public String getEmail(){ return email;}
    public String getImgUrl(){ return imgUrl; }
    public String getChatID(){ return chatID; }
    public String getRoomID(){ return roomID; }
    public void setChatID(String chatID){ this.chatID = chatID; }
    public void setRoomID(String roomID){ this.roomID = roomID; }

    protected static abstract class Init<T extends Init<T>> extends Message.Init<T> {
        protected String id;
        protected String name;
        protected String bioText;
        protected String email;
        protected String imgUrl;

        public T withId(String id) {
            this.id = id;
            return self();
        }

        public T withName(String name) {
            this.name = name;
            return self();
        }

        public T withBioText(String bioText) {
            this.bioText = bioText;
            return self();
        }

        public T withEmail(String email) {
            this.email = email;
            return self();
        }

        public T withImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
            return self();
        }

        public Volunteer build() {
            return new Volunteer(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }

    private Volunteer(Init<?> init) {
        super(init);
        this.id = init.id;
        this.name = init.name;
        this.bioText = init.bioText;
        this.email = init.email;
        this.imgUrl = init.imgUrl;
    }
}
