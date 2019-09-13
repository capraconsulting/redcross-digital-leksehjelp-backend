package no.capraconsulting.endpoints;

import no.capraconsulting.auth.JwtFilter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/isopen")
public class OpenLeksehjelpEndpoint {
    private static boolean IS_OPEN = false;
    private Logger LOG = LoggerFactory.getLogger(OpenLeksehjelpEndpoint.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getIsOpen() {
        JSONObject responseObject = new JSONObject().put("isopen", OpenLeksehjelpEndpoint.IS_OPEN);
        return Response.ok(responseObject.toString()).build();
    }

    @POST
    @JwtFilter
    @Produces({MediaType.APPLICATION_JSON})
    public Response toggleIsOpen() {
        OpenLeksehjelpEndpoint.IS_OPEN = !OpenLeksehjelpEndpoint.IS_OPEN;
        JSONObject responseObject = new JSONObject().put("isopen", OpenLeksehjelpEndpoint.IS_OPEN);
        if (IS_OPEN) {
            LOG.info("Leksehjelpen er åpnet");
        } else {
            LOG.info("Leksehjelpen er stengt");
        }
        return Response.ok(responseObject.toString()).build();
    }
}
