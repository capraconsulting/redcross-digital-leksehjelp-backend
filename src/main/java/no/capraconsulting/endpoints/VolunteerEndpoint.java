package no.capraconsulting.endpoints;

import no.capraconsulting.auth.JwtFilter;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import no.capraconsulting.db.Database;
import no.capraconsulting.auth.JwtFilter;
import javax.sql.RowSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import no.capraconsulting.utils.EndpointUtils;


import static no.capraconsulting.endpoints.VolunteerEndpoint.VOLUNTEER_PATH;

@Path(VOLUNTEER_PATH)
@JwtFilter
public final class VolunteerEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(VolunteerEndpoint.class);
    public static final String VOLUNTEER_PATH = "/volunteers";

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response listVolunteers(@Context ContainerRequestContext requestContext, @QueryParam("allVolunteers") boolean allVolunteers) {
        String query = "SELECT vo.id, vo.name, vo.bio_text, vo.email, vo.img_url, vo.role, STRING_AGG(ISNULL(su.subject, ''), ', ') as subjects " +
            "FROM Volunteers vo " +
        "LEFT OUTER JOIN VOLUNTEER_SUBJECTS vs ON vo.id=vs.volunteer_id " +
        "LEFT OUTER JOIN SUBJECTS su ON vs.subject_id=su.id " +
        "GROUP BY vo.id, vo.name, vo.bio_text, vo.email, vo.img_url, vo.role;";

        try {
            RowSet result = Database.INSTANCE.selectQuery(query);
            JSONArray payload = EndpointUtils.buildPayload(result);
            return Response.ok(payload.toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @GET
    @Path("/self")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSelf(@Context ContainerRequestContext requestContext) {
        String query =
            "SELECT id, name, bio_text, email, img_url, role " +
            "FROM Volunteers " +
            "WHERE id = ?";

        try {
            JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
            String oid = claims.getClaimValue("oid", String.class);
            RowSet result = Database.INSTANCE.selectQuery(query, oid);
            JSONArray temp = EndpointUtils.buildPayload(result);
            JSONObject payload = temp.getJSONObject(0);
            return Response.ok(payload.toString()).build();
        } catch (SQLException|MalformedClaimException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }



    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateVolunteer(@Context ContainerRequestContext requestContext, String payload) {
        if (payload.length() == 0) {
            payload = "{}";
        }
        JSONObject data = new JSONObject(payload);

        // TODO: REMOVE
        String placeholderUrl = "https://upload.wikimedia.org/wikipedia/commons/e/ee/Red_Cross_icon.svg";

        String query =
             "MERGE VOLUNTEERS " +
             "USING (SELECT ? AS uid) AS U " +
             "ON VOLUNTEERS.id = U.uid " +
             "WHEN MATCHED THEN " +
             "UPDATE " +
             "SET id = U.uid, " +
             "name = ?, " +
             "email = ?, " +
             "bio_text = ?, " +
             "img_url = ? " +
             "WHEN NOT MATCHED THEN " +
             "INSERT (id, name, email, bio_text, img_url) VALUES (U.uid, ?, ?, ?, ?);";
        try {
            JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
            String oid = claims.getClaimValue("oid", String.class);
            String email = claims.getClaimValue("preferred_username", String.class);
            String name = claims.getClaimValue("name", String.class);
            String bio = "";
            if (data.has("bioText")) {
                bio = data.getString("bioText");
            }
            if (data.has("name")) {
                name = data.getString("name");
            }
            if (data.has("email")) {
                email = data.getString("email");
            }
            System.out.println(bio);
            Database.INSTANCE.manipulateQuery(query, false, oid, name, email, bio, placeholderUrl,
                    name, email, bio, placeholderUrl);
            return Response.status(200).build();
        } catch (SQLException|MalformedClaimException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
       }
    }

    @GET
    @Path("subjects")
    public Response listSubjects(@Context ContainerRequestContext requestContext, @QueryParam("allVolunteers") boolean allVolunteers) {
        String query = "" +
            "SELECT Subjects.id, subject, is_mestring " +
            "FROM Subjects " +
            "JOIN Volunteer_Subjects ON Volunteer_Subjects.subject_id = Subjects.id " +
            "JOIN Volunteers ON Volunteer_Subjects.volunteer_id = Volunteers.id ";

        try {
            RowSet result;
            if (!allVolunteers) {
                JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
                String oid = claims.getClaimValue("oid", String.class);
                query += "WHERE Volunteers.id = ?";
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
    @Path("subjects")
    public Response addSubject(String payload, @Context ContainerRequestContext requestContext) {
        JSONObject data = new JSONObject(payload);
        JSONArray subjects = data.getJSONArray("subjects");

        // First delete the old subjects
        try {
            String query = "DELETE FROM Volunteer_Subjects where volunteer_id = ?";
            JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
            String oid = claims.getClaimValue("oid", String.class);
            Database.INSTANCE.manipulateQuery(query, false, oid);
        } catch (SQLException|MalformedClaimException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }

        // Then insert the new ones
        for (int i = 0; i < subjects.length(); i++) {
            int subjectID = subjects.getInt(i);

            String query = "INSERT INTO Volunteer_Subjects (subject_id, volunteer_id) VALUES (?, ?)";

            try {
                JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
                String oid = claims.getClaimValue("oid", String.class);
                Database.INSTANCE.manipulateQuery(query, false, subjectID, oid);
            } catch (SQLException|MalformedClaimException e) {
                LOG.error(e.getMessage());
                return Response.status(422).build();
           }
        }
        return Response.status(200).build();
    }
}
