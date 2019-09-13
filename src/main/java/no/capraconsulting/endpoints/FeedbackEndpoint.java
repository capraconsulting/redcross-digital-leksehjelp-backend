package no.capraconsulting.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.capraconsulting.db.Database;
import no.capraconsulting.utils.EndpointUtils;
import no.capraconsulting.auth.JwtFilter;
import javax.sql.RowSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import javax.ws.rs.core.Context;
import javax.ws.rs.container.ContainerRequestContext;

import static no.capraconsulting.endpoints.FeedbackEndpoint.FEEDBACK_PATH;

@Path(FEEDBACK_PATH)
public final class FeedbackEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(FeedbackEndpoint.class);
    public static final String FEEDBACK_PATH = "/feedback";
 
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response getAllFeedback(@QueryParam("includeAll") boolean includeAll, @Context ContainerRequestContext requestContext) {
        String query = "SELECT Feedback.id, feedback_text, student_grade, question_date, subject FROM FEEDBACK " +
                "JOIN Questions ON Feedback.question_id = Questions.id " +
                "JOIN Subjects ON Questions.subject_id = Subjects.id ";
        if (!includeAll) {
            query += "" +
                "JOIN Volunteer_Subjects ON volunteer_id = ? AND Questions.subject_id = Volunteer_Subjects.subject_id ";
        }

        try {
            RowSet result;
            if (!includeAll) {
                JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
                String oid = claims.getClaimValue("oid", String.class);
                result = Database.INSTANCE.selectQuery(query, oid); 
            } else {
                result = Database.INSTANCE.selectQuery(query); 
            }
            JSONArray payload = EndpointUtils.buildPayload(result);
            return Response.ok(payload.toString()).build();
        } catch (SQLException|MalformedClaimException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @POST
    @Path("/{feedbackID}/delete")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response deleteFeedback(@PathParam("feedbackID")int feedbackID) {

        String query = "DELETE FROM Feedback where id = ?";

        try {
            Database.INSTANCE.manipulateQuery(query, false, feedbackID);
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @POST
    @Path("/question/{questionID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response postFeedback(@PathParam("questionID")int questionID, String payload) {
        JSONObject data = new JSONObject(payload);

        String query = "INSERT INTO FEEDBACK(feedback_text, question_id) VALUES (?, ?)";

        try {
            Database.INSTANCE.manipulateQuery(query, false, data.getString("feedbackText"), questionID);
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @GET
    @Path("/question/{questionID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getFeedback(@PathParam("questionID")int questionID) {

        String query = "SELECT * FROM FEEDBACK WHERE question_id = ?";

        try {
            RowSet result = Database.INSTANCE.selectQuery(query, questionID);
            JSONArray payload = EndpointUtils.buildPayload(result);
            return Response.ok(payload.toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }
}
