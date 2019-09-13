package no.capraconsulting.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.sql.RowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import com.google.common.base.CaseFormat;
import no.capraconsulting.mail.MailService;
import no.capraconsulting.db.Database;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;

public final class EndpointUtils {

    private static Logger LOG = LoggerFactory.getLogger(EndpointUtils.class);

    /**
     * Utility method for automaticly building the payload, also changes from
     * sql_underscore_syntax to expectedFrontendPayloadSyntax, therefore the column
     * names are important
     *
     * @param result RowSet from Database.INSTANCE.selectQuery(...)
     * @return Formatted JSONArray of the query result
     */
    public static JSONArray buildPayload(RowSet result) throws SQLException {
        
        JSONArray payload = new JSONArray();
        ResultSetMetaData rsmd = result.getMetaData();

        while (result.next()) {
            JSONObject obj = new JSONObject();
            int columnCount = rsmd.getColumnCount(); 
            
            for (int i = 1; i <= columnCount; i++ ) {
                String name = rsmd.getColumnName(i);
                String formattedName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
                obj.put(formattedName, result.getObject(name)); 
            }

            payload.put(obj);
        }
        
        return payload;
    } 

    /**
     * Utility method for validationg a state transition, valid state transitions for
     * Q and A's can be found in the documentation
     *
     * @param questionID ID of the question which is being checked
     * @param newState newState as requested by the client
     * @return Formatted JSONArray of the query result
     */
    public static boolean validStateTransition(int questionID, int newState) {
        String query = "SELECT id, state, is_public FROM Questions WHERE id = ?";
        try {
            RowSet result = Database.INSTANCE.selectQuery(query, questionID);
            if (result.next()) {
                int oldState = result.getInt("state");
                boolean isPublic = result.getBoolean("is_public");

                // Always OK
                if (oldState == newState) return true;

                if (newState == 1) {
                    LOG.debug("Q&As can never be moved to state 1");
                    return false;
                }
                if ((oldState == 1) && (newState != 2)) {
                    LOG.debug("Q&As cannot be approved/published without an answer");
                    return false;
                }
                if (oldState == 2 && (newState != 3)) {
                    LOG.debug("Q&As needs approval before they are published");
                    return false;
                }
                if (newState == 5 && !isPublic) {
                    LOG.debug("Q&As that are not public can't be published");
                    return false;
                }
                return true;
            } 
            return false;
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * Utility for sending the email to the student, and deleting
     * the stored email
     *
     * @param questionID ID of the question which is being checked
     * @param newState newState as requested by the client
     * @return Formatted JSONArray of the query result
     */
    public static void sendMail(int questionID) {
        // Should send the mail and delete it after
        String selectQuery = "" +
            "SELECT * FROM Questions " +
            "JOIN Emails ON Emails.question_id = Questions.id " +
            "JOIN Answers ON Answers.question_id = Questions.id " +
            "WHERE Questions.id = ?";
        String deleteQuery = "DELETE FROM Emails where question_id = ?";

        try {
            RowSet result = Database.INSTANCE.selectQuery(selectQuery, questionID);
            Database.INSTANCE.manipulateQuery(deleteQuery, false, questionID);

            if (result.next()) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Callable<Response> mailService = new MailService(
                        result.getString("email"),
                        result.getString("title"),
                        result.getString("answer_text"));

                executor.submit(mailService);
                executor.shutdown();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }
}
