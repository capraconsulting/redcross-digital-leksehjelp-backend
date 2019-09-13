package no.capraconsulting.utils;

import com.google.gson.Gson;
import no.capraconsulting.chatmessages.SocketMessage;

public class ChatUtils {
    public static String stringify(SocketMessage msg) {

        Gson gson = new Gson();
        return gson.toJson(msg);
    }
}
