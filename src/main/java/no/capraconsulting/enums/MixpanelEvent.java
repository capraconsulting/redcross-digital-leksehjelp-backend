package no.capraconsulting.enums;

import no.capraconsulting.chat.StudentInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import static no.capraconsulting.enums.Chat.ChatTypeEnum.LEKSEHJELP_TEXT;

public enum MixpanelEvent {
    STUDENT_ENTERED_QUEUE("Bedt om leksehjelp", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("type", formatChatType(studentInfo.getChatType()));
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        return props;
    }),
    STUDENT_LEFT_QUEUE("Forlot leksehjelp-kø", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", studentInfo.getGrade());
        props.put("minutterIKø", TimeUnit.MILLISECONDS.toMinutes(duration));
        return props;
    }),
    STUDENT_SENT_NEW_QUESTION("Nytt spørsmål stilt", (data) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", formatGrade(data.getInt("studentGrade")));
        props.put("fag", data.getString("subject"));
        props.put("publiseringTillatt", data.getBoolean("isPublic"));
        if (data.has("themes")) {
            List<String> tema = new ArrayList<>();
            JSONArray themes = data.getJSONArray("themes");
            for (int i = 0; i < themes.length(); i++) {
                JSONObject theme = themes.getJSONObject(i);
                tema.add(theme.getString("theme"));
            }
            props.put("tema", tema);
        }
        return props;
    }),
    VOLUNTEER_FINISHED_HELP("Hjulpet elev", (studentInfo, duration) -> {//TODO: Sandra, må kalles når du får tak i antall minutter
        JSONObject props = new JSONObject();
        props.put("Minutter i samtale", duration);
        props.put("type", formatChatType(studentInfo.getChatType()));
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        if (studentInfo.getThemes() != null) {
            props.put("tema", studentInfo.getThemes().toString());
        }
        return props;
    }),
    VOLUNTEER_APPROVED_QUESTION("Spørsmål besvart", (data) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", formatGrade(data.getInt("studentGrade")));
        props.put("fag", data.getString("subject"));
        if (data.has("themesNames")) {
            List<String> tema = new ArrayList<>();
            JSONArray themes = data.getJSONArray("themesNames");
            for (int i = 0; i < themes.length(); i++) {
                tema.add(themes.getString(i));
            }

            props.put("tema", tema);
        }
        return props;
    }),
    VOLUNTEER_STARTED_HELP("Start leksehjelp", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("sekunderIKø", TimeUnit.MILLISECONDS.toSeconds(duration));
        props.put("type", formatChatType(studentInfo.getChatType()));
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        if (studentInfo.getThemes() != null) {
            props.put("tema", studentInfo.getThemes().toString());
        }
        return props;
    }),
    VOLUNTEER_REMOVED_STUDENT_FROM_QUEUE("Fjernet fra køen av frivillig", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("sekunderIKø", TimeUnit.MILLISECONDS.toSeconds(duration));
        props.put("type", formatChatType(studentInfo.getChatType()));
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        if (studentInfo.getThemes() != null) {
            props.put("tema", studentInfo.getThemes().toString());
        }
        return props;
    });

    private final String text;
    public BiFunction<StudentInfo, Long, JSONObject> getPropsFromStudentInfo;
    public Function<JSONObject, JSONObject> getPropsFromJSONObject;

    MixpanelEvent(final String text, BiFunction<StudentInfo, Long, JSONObject> getPropsFromStudentInfo) {
        this.text = text;
        this.getPropsFromStudentInfo = getPropsFromStudentInfo;
    }

    MixpanelEvent(final String text, Function<JSONObject, JSONObject> getPropsFromJSONObject) {
        this.text = text;
        this.getPropsFromJSONObject = getPropsFromJSONObject;
    }

    @Override
    public String toString() {
        return text;
    }

    private static String formatChatType(Chat.ChatTypeEnum type) {
        switch (type) {
            case LEKSEHJELP_TEXT:
            case MESTRING_TEXT:
                return "chat";
            case LEKSEHJELP_VIDEO:
            case MESTRING_VIDEO:
                return "videochat";
        }

        return type.equals(LEKSEHJELP_TEXT) ? "chat" : "videochat";
    }

    private static String formatGrade(int gradeNumber) {
        switch (gradeNumber) {
            case 8:
                return "8. klasse";
            case 9:
                return "9. klasse";
            case 10:
                return "10. klasse";
            case 11:
                return "VG1";
            case 12:
                return "VG2";
            case 13:
                return "VG3";
            default:
                throw new IllegalArgumentException("Invalid student grade: " + gradeNumber);
        }
    }
}

