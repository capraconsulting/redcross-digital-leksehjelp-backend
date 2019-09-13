package no.capraconsulting.endpoints;

import no.capraconsulting.enums.MixpanelEvent;
import no.capraconsulting.mixpanel.MixpanelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.capraconsulting.db.Database;
import no.capraconsulting.utils.EndpointUtils;
import no.capraconsulting.auth.JwtFilter;
import javax.sql.RowSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Timestamp;
import java.util.Map;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import javax.ws.rs.core.Context;
import javax.ws.rs.container.ContainerRequestContext;

import static no.capraconsulting.endpoints.QuestionEndpoint.QUESTION_PATH;

@Path(QUESTION_PATH)
public final class QuestionEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(QuestionEndpoint.class);
    private MixpanelService mixpanelService = new MixpanelService();
    public static final String QUESTION_PATH = "/questions";

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response getQuestionsList(@QueryParam("state")int state, @QueryParam("includeAll") boolean includeAll,
            @Context ContainerRequestContext requestContext) {
        String query =
            "SELECT Questions.id as qid, title, question_text, student_grade, subject, question_date, theme, " +
            "Question_Themes.theme_id as tid " +
            "FROM Questions " +
            "JOIN Subjects ON Questions.subject_id = Subjects.id " +
            "LEFT JOIN Question_Themes ON Question_Themes.question_id = Questions.id " +
            "LEFT JOIN Themes ON Question_Themes.theme_id = Themes.id ";
        if (!includeAll) {
            query += "JOIN Volunteer_Subjects ON volunteer_id = ? AND Questions.subject_id = Volunteer_Subjects.subject_id ";
        }
        query += "WHERE Questions.state = ? ";

        try {
            RowSet result;
            if (includeAll) {
                result  = Database.INSTANCE.selectQuery(query, state);
            } else {
                JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
                String oid = claims.getClaimValue("oid", String.class);
                result  = Database.INSTANCE.selectQuery(query, oid, state);
            }
            Map<Integer, JSONObject> questions = new HashMap<Integer, JSONObject>();

            while (result.next()) {
                int id = result.getInt("qid");

                JSONObject question = questions.getOrDefault(id, new JSONObject());

                // Aggregate the themes
                JSONArray themes = (question.has("themes")) ?
                    (JSONArray) question.get("themes") : new JSONArray();
                JSONObject theme = new JSONObject();
                int themeID = result.getInt("tid");
                theme.put("id", themeID);
                theme.put("theme", result.getString("theme"));
                if (themeID != 0) themes.put(theme);

                question.put("id", result.getString("qid"));
                question.put("title", result.getString("title"));
                question.put("questionText", result.getString("question_text"));
                question.put("questionDate", result.getString("question_date"));
                question.put("studentGrade", result.getString("student_grade"));
                question.put("subject", result.getString("subject"));
                question.put("themes", themes);
                questions.put(id, question);
            }
            return Response.ok(questions.values().toString()).build();
        } catch (SQLException|MalformedClaimException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @GET
    @Path("/{questionID}")
    @Produces({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response getQuestion(@PathParam("questionID")int questionID) {
        String query =
            "SELECT Questions.id as qid, title, question_text, state, student_grade, subject, question_date, theme, " +
            "Question_Themes.theme_id as tid, is_public, answer_text, answer_date, Files.id as fid, share, directory, name, url, is_sent = " +
            "CASE WHEN Emails.id IS NULL THEN 1 ELSE 0 END " +
            "FROM Questions " +
            "JOIN Subjects ON Questions.subject_id = Subjects.id " +
            "LEFT JOIN Emails ON Questions.id = Emails.question_id " +
            "LEFT JOIN Files ON Questions.id = Files.question_id " +
            "LEFT JOIN Answers ON Questions.id = Answers.question_id " +
            "LEFT JOIN Question_Themes ON Question_Themes.question_id = Questions.id " +
            "LEFT JOIN Themes ON Question_Themes.theme_id = Themes.id " +
            "WHERE Questions.id = ? ";

        try {
            RowSet result = Database.INSTANCE.selectQuery(query, questionID);
            Map<Integer, JSONObject> questions = new HashMap<Integer, JSONObject>();
            ArrayList<Integer> includedFiles = new ArrayList<Integer>();
            ArrayList<Integer> includedThemes = new ArrayList<Integer>();

            while (result.next()) {
                int id = result.getInt("qid");

                JSONObject question = questions.getOrDefault(id, new JSONObject());

                // Aggregate the files
                JSONArray files = (question.has("files")) ?
                    (JSONArray) question.get("files") : new JSONArray();
                JSONObject file  = new JSONObject();
                int fileID = result.getInt("fid");
                file.put("share", result.getString("share"));
                file.put("directory", result.getString("directory"));
                file.put("fileName", result.getString("name"));
                file.put("fileUrl", result.getString("url"));
                if (fileID != 0 && !includedFiles.contains(fileID)) {
                    files.put(file);
                    includedFiles.add(fileID);
                }

                // Aggregate the themes
                JSONArray themes = (question.has("themes")) ?
                    (JSONArray) question.get("themes") : new JSONArray();
                JSONObject theme = new JSONObject();
                int themeID = result.getInt("tid");
                theme.put("id", themeID);
                theme.put("theme", result.getString("theme"));
                if (themeID != 0 && !includedThemes.contains(themeID)) {
                    themes.put(theme);
                    includedThemes.add(themeID);
                }

                question.put("id", result.getString("qid"));
                question.put("title", result.getString("title"));
                question.put("state", result.getString("state"));
                question.put("questionText", result.getString("question_text"));
                question.put("questionDate", result.getString("question_date"));
                question.put("studentGrade", result.getString("student_grade"));
                question.put("subject", result.getString("subject"));
                question.put("answerText", result.getString("answer_text"));
                question.put("answerDate", result.getString("answer_date"));
                question.put("isSent", result.getString("is_sent"));
                question.put("isPublic", result.getString("is_public"));
                question.put("files", files);
                question.put("themes", themes);
                questions.put(id, question);
            }
            JSONObject temp = questions.getOrDefault(questionID, new JSONObject());
            return Response.ok(temp.toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response postQuestion(String payload) {
        JSONObject data = new JSONObject(payload);
        mixpanelService.trackEvent(MixpanelEvent.STUDENT_SENT_NEW_QUESTION, data);

        String queryQuestion = "INSERT INTO "
                      + "QUESTIONS(question_text, subject_id, question_date, student_grade, is_public) "
                      + "VALUES (?,?,?,?,?)";
        String queryEmail = "INSERT INTO "
                      + "EMAILS(email, question_id) "
                      + "VALUES (?,?)";
        String queryFile = "INSERT INTO "
                      + "FILES(share, directory, name, url, question_id) "
                      + "VALUES (?,?,?,?,?)";
        String queryThemes = "INSERT INTO "
                      + "QUESTION_THEMES(theme_id, question_id) "
                      + "VALUES (?,?)";
        try {
            int questionID = Database.INSTANCE.manipulateQuery(queryQuestion, true,
                    data.getString("questionText"),
                    data.getInt("subjectID"),
                    new Timestamp(System.currentTimeMillis()),
                    data.getInt("studentGrade"),
                    data.getBoolean("isPublic"));
            Database.INSTANCE.manipulateQuery(queryEmail, false,
                    data.getString("email"),
                    questionID);
            if (data.has("files")) {
                JSONArray files = data.getJSONArray("files");
                for (int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    Database.INSTANCE.manipulateQuery(queryFile, false,
                            file.getString("share"),
                            file.getString("directory"),
                            file.getString("fileName"),
                            file.getString("fileUrl"),
                            questionID);
                }
            }
            if (data.has("themes")) {
                JSONArray themes = data.getJSONArray("themes");
                for (int i = 0; i < themes.length(); i++) {
                    JSONObject theme = themes.getJSONObject(i);
                    Database.INSTANCE.manipulateQuery(queryThemes, false,
                            theme.getInt("id"), questionID);
                }
            }
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }


    @GET
    @Path("/public")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchQuestions(@QueryParam("searchText")String searchText, @QueryParam("orderByDate")boolean orderByDate,
            @QueryParam("subjectID")String subjectID, @QueryParam("page")int page, @QueryParam("grade")String grade)  {

        String query = "";

        if (searchText == null) {
            query += "" +
            // Select relevant columns
            "SELECT Questions.id as qid, title, question_text, student_grade, subject, question_date, answer_text, answer_date, " +
            "theme, Question_Themes.theme_id as tid, row_count = COUNT(*) OVER() " +
            "FROM Questions ";
        } else {
            query += "" +
            // Select relevant columns
            "SELECT Questions.id as qid, title, question_text, student_grade, subject, question_date, answer_text, answer_date, " +
            "theme, Question_Themes.theme_id as tid, row_count = COUNT(*) OVER(), RANK " +
            "FROM Questions " +
            // Search in Questions.question_text and Answers.answer_text columns
            "JOIN (" +
            "SELECT Questions.id FROM Questions JOIN FREETEXTTABLE (Questions, question_text, ?) AS FTSQuestions ON FTSQuestions.[KEY] = Questions.id " +
            "UNION " +
            "SELECT Answers.question_id FROM Answers JOIN FREETEXTTABLE (Answers, answer_text, ?) AS FTSAnswers ON FTSAnswers.[KEY] = Answers.id " +
            ") FTS ON Questions.id = FTS.id " +
            "LEFT JOIN FREETEXTTABLE (Questions, question_text, ?) AS FTSQuestions ON FTSQuestions.[KEY] = Questions.id ";
        }

        query += "" +
        "JOIN Subjects ON Questions.subject_id = Subjects.id " +
        "JOIN Answers ON Questions.id = Answers.question_id " +
        "LEFT JOIN Question_Themes ON Question_Themes.question_id = Questions.id " +
        "LEFT JOIN Themes ON Question_Themes.theme_id = Themes.id " +
        "WHERE Questions.is_public = 1 " +
        "AND Questions.state = 5 ";

        if (subjectID != null) {
            int id = Integer.parseInt(subjectID);
            query += "AND Subjects.id = " + id + " ";
        }

        if (grade != null) {
            int id = Integer.parseInt(grade);
            query += "AND Questions.student_grade = " + id + " ";
        }

        if (orderByDate || searchText == null) {
            query += "ORDER BY question_date DESC ";
        } else {
            query += "ORDER BY RANK DESC ";
        }

        query += "OFFSET " + (page - 1) * 10 + " ROWS FETCH NEXT 10 ROWS ONLY";

        try {
            RowSet result;
            if (searchText == null) {
                result = Database.INSTANCE.selectQuery(query);
            } else {
                result = Database.INSTANCE.selectQuery(query, searchText, searchText, searchText);
            }
            Map<Integer, JSONObject> questions = new LinkedHashMap<Integer, JSONObject>();
            while (result.next()) {
                int id = result.getInt("qid");

                JSONObject question = questions.getOrDefault(id, new JSONObject());

                // Aggregate the themes
                JSONArray themes = (question.has("themes")) ?
                    (JSONArray) question.get("themes") : new JSONArray();
                JSONObject theme = new JSONObject();
                int themeID = result.getInt("tid");
                theme.put("id", themeID);
                theme.put("theme", result.getString("theme"));
                if (themeID != 0) themes.put(theme);

                question.put("id", result.getString("qid"));
                question.put("title", result.getString("title"));
                question.put("questionText", result.getString("question_text"));
                question.put("questionDate", result.getString("question_date"));
                question.put("studentGrade", result.getString("student_grade"));
                question.put("subject", result.getString("subject"));
                question.put("answerText", result.getString("answer_text"));
                question.put("answerDate", result.getString("answer_date"));
                question.put("totalRows", result.getString("row_count"));
                question.put("themes", themes);
                questions.put(id, question);
            }
            return Response.ok(questions.values().toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @POST
    @Path("/{questionID}")
    @Produces({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response postEdit(@PathParam("questionID")int questionID, String payload) {
        // Should update the answer or create it if it doesn't exist
        JSONObject data = new JSONObject(payload);

        // Check if the state transistion is valid
        int new_state = data.getInt("state");
        if (!EndpointUtils.validStateTransition(questionID, new_state)) {
            return Response.status(422).build();
        }

        try {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            if (new_state == 2) {
                // Edit the question
                String answerQuery = "" +
                    "UPDATE Answers SET answer_text = ?, answer_date = ? WHERE question_id = ? " +
                    "IF @@ROWCOUNT=0 " +
                    "INSERT INTO ANSWERS (answer_text, answer_date, question_id) VALUES (?, ?, ?)";
                String questionQuery = "UPDATE Questions " +
                    "SET state = ?, title = ?, question_text = ? WHERE id = ?";
                String deleteThemes = "DELETE FROM Question_Themes WHERE question_id = ? ";
                String addThemes = "INSERT INTO "
                              + "Question_Themes(theme_id, question_id) "
                              + "VALUES (?,?)";

                // Update the answer table
                String answerText = data.getString("answerText");
                Database.INSTANCE.manipulateQuery(answerQuery, false,
                        answerText, currentTime, questionID,
                        answerText, currentTime, questionID);

                // Update the question table
                String title = data.getString("title");
                String questionText = data.getString("questionText");
                Database.INSTANCE.manipulateQuery(questionQuery, false,
                        new_state, title, questionText, questionID);

                // Update the themes
                Database.INSTANCE.manipulateQuery(deleteThemes, false, questionID);
                JSONArray themes = data.getJSONArray("themes");
                for (int i = 0; i < themes.length(); i++) {
                    int themeID = themes.getInt(i);
                    Database.INSTANCE.manipulateQuery(addThemes, false, themeID, questionID);
                }
            } else {
                // Only update the state
                String questionQuery = "UPDATE Questions SET state = ? WHERE id = ?";
                Database.INSTANCE.manipulateQuery(questionQuery, false, new_state, questionID);
            }

            // Send the mail if the question was approved
            if (new_state == 4) {
                EndpointUtils.sendMail(questionID);
                mixpanelService.trackEvent(MixpanelEvent.VOLUNTEER_APPROVED_QUESTION, data);
            }

            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @GET
    @Path("/public/{questionID}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPublicQuestion(@PathParam("questionID")int questionID) {
        String query =
            "SELECT Questions.id as qid, title, question_text, state, student_grade, subject, question_date, theme, " +
            "Question_Themes.theme_id as tid, is_public, answer_text, answer_date, share, directory, name, url " +
            "FROM Questions " +
            "JOIN Subjects ON Questions.subject_id = Subjects.id " +
            "LEFT JOIN Files ON Questions.id = Files.question_id " +
            "LEFT JOIN Answers ON Questions.id = Answers.question_id " +
            "LEFT JOIN Question_Themes ON Question_Themes.question_id = Questions.id " +
            "LEFT JOIN Themes ON Question_Themes.theme_id = Themes.id " +
            "WHERE Questions.id = ? " +
            "AND Questions.is_public = 1 " +
            "AND Questions.state = 5 ";

        try {
            RowSet result = Database.INSTANCE.selectQuery(query, questionID);
            Map<Integer, JSONObject> questions = new HashMap<Integer, JSONObject>();

            while (result.next()) {
                int id = result.getInt("qid");

                JSONObject question = questions.getOrDefault(id, new JSONObject());

                // Aggregate the files
                JSONArray files = (question.has("files")) ?
                    (JSONArray) question.get("files") : new JSONArray();
                JSONObject file  = new JSONObject();
                file.put("share", result.getString("share"));
                file.put("directory", result.getString("directory"));
                file.put("fileName", result.getString("name"));
                file.put("fileUrl", result.getString("url"));
                if (file.length() != 0) files.put(file);

                // Aggregate the themes
                JSONArray themes = (question.has("themes")) ?
                    (JSONArray) question.get("themes") : new JSONArray();
                JSONObject theme = new JSONObject();
                int themeID = result.getInt("tid");
                theme.put("id", themeID);
                theme.put("theme", result.getString("theme"));
                if (themeID != 0) themes.put(theme);

                question.put("id", result.getString("qid"));
                question.put("title", result.getString("title"));
                question.put("state", result.getString("state"));
                question.put("questionText", result.getString("question_text"));
                question.put("questionDate", result.getString("question_date"));
                question.put("studentGrade", result.getString("student_grade"));
                question.put("subject", result.getString("subject"));
                question.put("answerText", result.getString("answer_text"));
                question.put("answerDate", result.getString("answer_date"));
                question.put("files", files);
                question.put("themes", themes);
                questions.put(id, question);
            }
            JSONObject temp = questions.getOrDefault(questionID, new JSONObject());
            return Response.ok(temp.toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }
}
