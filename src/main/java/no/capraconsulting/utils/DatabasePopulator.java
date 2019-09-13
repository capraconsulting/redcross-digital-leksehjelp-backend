package no.capraconsulting.utils;
import no.capraconsulting.db.Database;
import no.capraconsulting.db.DatabaseInitializer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;
import java.sql.*;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

@Path("/populate")
public class DatabasePopulator {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response questions(@QueryParam("stressTest")int stressTest, @QueryParam("includeString")String includeString) {


        String insertSubject1 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Norsk', 0)";
        String insertSubject2 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Matte', 0)";
        String insertSubject3 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Naturfag', 0)";
        String insertSubject4 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Engelsk', 0)";
        String insertSubject5 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Fysikk', 0)";
        String insertSubject6 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('KRLE', 0)";
        String insertSubject7 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Eksamensnerver', 1)";
        String insertSubject8 = "INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Stress', 1)";

        String insertQuestion1 = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id) " +
            "VALUES ('', 'Hva er 2+2?', 1, 10, 1, 2)";
        String insertQuestion2 = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id) " +
            "VALUES ('Dikt er vanskelig', 'How to dikt?', 1, 11, 5, 1)";
        String insertQuestion3 = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id) " +
            "VALUES ('', 'Stresset for eksamen...', 0, 12, 3, 1)";
        String insertQuestion4 = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id) " +
            "VALUES ('Norsk greier', 'How to sakprosa?', 1, 12, 4, 1)";
        String insertQuestion5 = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id) " +
            "VALUES ('Mattespørsmål', 'Hva er 2*2?', 1, 12, 5, 2)";

        String insertAnswer1 = "INSERT INTO ANSWERS (answer_text, question_id) " +
            "VALUES ('Dikt i vei', 2)";
        String insertAnswer2 = "INSERT INTO ANSWERS (answer_text, question_id) " +
            "VALUES ('Godt spørsmål.', 4)";
        String insertAnswer3 = "INSERT INTO ANSWERS (answer_text, question_id) " +
            "VALUES ('4', 5)";
        
        String insertFeedback1 = "INSERT INTO FEEDBACK (question_id, feedback_text) " +
            "VALUES (4, 'Det der er ikke et ordentlig svar')";

        String insertTheme1 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Dikt', 1)";
        String insertTheme2 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Nynorsk', 1)";
        String insertTheme3 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Sakprosa', 1)";
        String insertTheme4 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Analyse', 1)";
        String insertTheme5 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Algebra', 2)";
        String insertTheme6 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Konstruksjon', 2)";
        String insertTheme7 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Brøk', 2)";
        String insertTheme8 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Sannsynlighet', 2)";
        String insertTheme9 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Derivasjon', 2)";
        String insertTheme10 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Integraler', 2)";
        String insertTheme11 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Darwin', 3)";
        String insertTheme12 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Klimaendringer', 3)";
        String insertTheme13 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Grammatikk', 4)";
        String insertTheme14 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Kultur', 4)";
        String insertTheme15 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Tyngdekraft', 5)";
        String insertTheme16 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Relativitetsteori', 5)";
        String insertTheme17 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Mekanikk', 5)";
        String insertTheme18 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Bibelen', 6)";
        String insertTheme19 = "INSERT INTO THEMES (theme, subject_id) VALUES ('Filosofi', 6)";

        String insertQuestionThemes1 = "INSERT INTO QUESTION_THEMES (question_id, theme_id) VALUES (2, 1)";

        // Init the database
        DatabaseInitializer.init();
        
        // Fill the database with some dummy data
        try {
                Connection con = Database.INSTANCE.getConnection();
                Statement stmt = con.createStatement();

                stmt.addBatch(insertSubject1);
                stmt.addBatch(insertSubject2);
                stmt.addBatch(insertSubject3);
                stmt.addBatch(insertSubject4);
                stmt.addBatch(insertSubject5);
                stmt.addBatch(insertSubject6);
                stmt.addBatch(insertSubject7);
                stmt.addBatch(insertSubject8);
                stmt.addBatch(insertQuestion1);
                stmt.addBatch(insertQuestion2);
                stmt.addBatch(insertQuestion3);
                stmt.addBatch(insertQuestion4);
                stmt.addBatch(insertQuestion5);
                stmt.addBatch(insertAnswer1);
                stmt.addBatch(insertAnswer2);
                stmt.addBatch(insertAnswer3);
                stmt.addBatch(insertTheme1);
                stmt.addBatch(insertTheme2);
                stmt.addBatch(insertTheme3);
                stmt.addBatch(insertTheme4);
                stmt.addBatch(insertTheme5);
                stmt.addBatch(insertTheme6);
                stmt.addBatch(insertTheme7);
                stmt.addBatch(insertTheme8);
                stmt.addBatch(insertTheme9);
                stmt.addBatch(insertTheme10);
                stmt.addBatch(insertTheme11);
                stmt.addBatch(insertTheme12);
                stmt.addBatch(insertTheme13);
                stmt.addBatch(insertTheme14);
                stmt.addBatch(insertTheme15);
                stmt.addBatch(insertTheme16);
                stmt.addBatch(insertTheme17);
                stmt.addBatch(insertTheme18);
                stmt.addBatch(insertTheme19);
                stmt.addBatch(insertQuestionThemes1);
                stmt.addBatch(insertFeedback1);
                stmt.executeBatch();
                con.commit();

                String response = "DB initiliazed and populated.";
                return Response.ok(response).build();
            } catch (SQLException e) {
                e.printStackTrace();
                String response = "Failed.";
                return Response.ok(response).build();
            }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("stress")
    public Response stressTest(@QueryParam("total")int total) {

        File file = new File("wordlist");
        ArrayList<String> words = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                words.add(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String insertQuestion = "INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id, question_date) " +
            "VALUES (?, ?, 1, 10, 5, 2, ?)";

        String insertAnswer = "INSERT INTO ANSWERS (answer_text, question_id, answer_date) " +
            "VALUES (?, ?, ?)";
        
        try {
            int i= 0;
            Random rand = new Random();

            while (i++ < total) {
                int numWords =  10 + rand.nextInt(50);
                String titleText = "";
                String questionText = "";
                String answerText = "";
                for (int j = 0; j < numWords; j++) {
                    questionText += words.get(rand.nextInt(words.size()-1)) + " "; 
                    answerText += words.get(rand.nextInt(words.size()-1)) + " "; 
                    if (j % 10 == 0) {
                        titleText += words.get(rand.nextInt(words.size()-1)) + " "; 
                    }
                }
                int id = Database.INSTANCE.manipulateQuery(insertQuestion, true,
                        titleText, questionText, new Timestamp(System.currentTimeMillis()));
                Database.INSTANCE.manipulateQuery(insertAnswer, false,
                        answerText, id, new Timestamp(System.currentTimeMillis()));

            }
                String response = "DB initiliazed and populated.";
                return Response.ok(response).build();
            } catch (SQLException e) {
                e.printStackTrace();
                String response = "Failed.";
                return Response.ok(response).build();
            }
    }
}
