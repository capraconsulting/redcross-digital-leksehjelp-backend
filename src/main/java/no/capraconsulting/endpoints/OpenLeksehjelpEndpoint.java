package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import no.capraconsulting.utils.EndpointUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/information")
public class OpenLeksehjelpEndpoint {
    private Logger LOG = LoggerFactory.getLogger(OpenLeksehjelpEndpoint.class);
    private static final Gson gson = new Gson();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/isopen")
    public Response getIsOpen() {
        String query = "SELECT JSON_VALUE(data, '$.isOpen') AS 'isOpen' FROM INFORMATION";

        try {
            JSONObject response = EndpointUtils.getWithQuery(query);
            return Response.ok(response.toString()).build();
        } catch (SQLException e) {
           LOG.error(e.getMessage());
           return Response.status(422).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInformation() {
        String query = "SELECT data FROM INFORMATION;";

        try {
            JSONObject payload  = EndpointUtils.getWithQuery(query);
            return Response.ok(payload.get("data")).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }
}
