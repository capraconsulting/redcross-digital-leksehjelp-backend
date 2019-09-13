package no.capraconsulting.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.capraconsulting.db.Database;
import no.capraconsulting.utils.EndpointUtils;
import no.capraconsulting.auth.JwtFilter;
import javax.sql.RowSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

import static no.capraconsulting.endpoints.TimeslotEndpoint.TIMESLOT_PATH;

@Path(TIMESLOT_PATH)
public final class TimeslotEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(TimeslotEndpoint.class);
    public static final String TIMESLOT_PATH = "/timeslots";

    @GET
    @Path("/subject/{subjectID}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTimeslots(@PathParam("subjectID")int subjectID) {
        String query = "" +
            "SELECT day, from_time, to_time FROM TIMESLOTS " +
            "WHERE subject_id = ?";

        try {
            RowSet result = Database.INSTANCE.selectQuery(query, subjectID);
            JSONArray payload = EndpointUtils.buildPayload(result);
            return Response.ok(payload.toString()).build(); 
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }

    }

    @POST
    @Path("/subject/{subjectID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response postTimeslots(String payload, @PathParam("subjectID")int subjectID){

        JSONObject data = new JSONObject(payload);
    
        String query = "INSERT INTO "
                      + "TIMESLOTS (day, from_time, to_time, subject_id) "
                      + "VALUES (?,?,?,?)";

        try {
            Database.INSTANCE.manipulateQuery(
                query, 
                false, 
                data.getInt("day"), 
                data.getString("from_time"), 
                data.getString("to_time"),
                subjectID
            );
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
       }
    }
}
