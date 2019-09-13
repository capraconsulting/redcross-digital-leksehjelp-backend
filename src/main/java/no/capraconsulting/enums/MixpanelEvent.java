package no.capraconsulting.enums;

import no.capraconsulting.chat.StudentInfo;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum MixpanelEvent {
    STUDENT_ENTERED_QUEUE("Elev ba om leksehjelp", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("type", studentInfo.getChatType());
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        return props;
    }),
    STUDENT_LEFT_QUEUE("Elev forlot leksehjelpkø", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", studentInfo.getGrade());
        props.put("minutterIKø", TimeUnit.MILLISECONDS.toMinutes(duration));
        return props;
    }),
    STUDENT_SENT_NEW_QUESTION("Nytt spørsmål stilt av elev", (data) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", data.getInt("studentGrade"));
        props.put("fag", data.getString("subject"));
        props.put("publiseringTillatt", data.getBoolean("isPublic"));
        if (data.has("themes")) {
            props.put("tema", data.getJSONArray("themes"));
        }
        return props;
    }),
    VOLUNTEER_FINISHED_HELP("Frivillig har hjulpet elev", (studentInfo, duration) -> {//TODO: Sandra, må kalles når du får tak i antall minutter
        JSONObject props = new JSONObject();
        props.put("minutterISamtale", duration);
        props.put("type", studentInfo.getChatType());
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        if (studentInfo.getThemes() != null) {
            props.put("tema", studentInfo.getThemes().toString());
        }
        return props;
    }),
    VOLUNTEER_APPROVED_QUESTION("Frivillig godkjente besvarelse", (data) -> {
        JSONObject props = new JSONObject();
        props.put("trinn", data.getString("studentGrade"));
        props.put("fag", data.getString("subject"));
        if (data.has("themes")) {
            props.put("tema", data.getJSONArray("themes"));
        }
        return props;
    }),
    VOLUNTEER_STARTED_HELP("Frivillig startet leksehjelp", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("sekunderIKø", TimeUnit.MILLISECONDS.toSeconds(duration));
        props.put("type", studentInfo.getChatType());
        props.put("fag", studentInfo.getSubject());
        props.put("trinn", studentInfo.getGrade());
        if (studentInfo.getThemes() != null) {
            props.put("tema", studentInfo.getThemes().toString());
        }
        return props;
    }),
    VOLUNTEER_REMOVED_STUDENT_FROM_QUEUE("Frivillig fjernet elev fra kø", (studentInfo, duration) -> {
        JSONObject props = new JSONObject();
        props.put("sekunderIKø", TimeUnit.MILLISECONDS.toSeconds(duration));
        props.put("type", studentInfo.getChatType());
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
}
