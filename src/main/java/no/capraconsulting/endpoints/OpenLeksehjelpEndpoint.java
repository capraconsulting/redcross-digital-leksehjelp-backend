package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.db.Database;
import no.capraconsulting.domain.Information;
import no.capraconsulting.utils.EndpointUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;

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

    @PUT
    @JwtFilter
    @Path("/announcement")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateAnnouncement(String payload) {
        JSONObject data = new JSONObject(payload);

        try {
            String insertInformation = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$.announcement', ?)";
            Database.INSTANCE.manipulateQuery(insertInformation, false, data.getString("announcement"));

            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @PUT
    @JwtFilter
    @Path("/open")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateIsOpen(String payload) {
        JSONObject data = new JSONObject(payload);
        String insertInformation = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$.isOpen', ?)";

        try {
            Database.INSTANCE.manipulateQuery(insertInformation, false, data.getBoolean("isOpen"));
            return Response.ok(data.toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @PUT
    @JwtFilter
    @Path("/openinghours")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateOpeningHours(String payload) {
        Information information = gson.fromJson(payload, Information.class);
        List<String> openingDays = Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "other");

        openingDays.forEach(openingDay -> {
            try {
                if (!openingDay.equals("other")) {
                    String insertStart = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$."+openingDay+".start', ?)";
                    String insertEnd = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$."+openingDay+".end', ?)";
                    String insertEnabled = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$."+openingDay+".enabled', ?)";

                    Database.INSTANCE.manipulateQuery(insertStart, false, information.getOpeningHourByDay(openingDay).getStart());
                    Database.INSTANCE.manipulateQuery(insertEnd, false, information.getOpeningHourByDay(openingDay).getEnd());
                    Database.INSTANCE.manipulateQuery(insertEnabled, false, information.getOpeningHourByDay(openingDay).isEnabled());

                } else {
                    String insertMessage = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$."+openingDay+".message', ?)";
                    String insertEnabled = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$."+openingDay+".enabled', ?)";
                    Database.INSTANCE.manipulateQuery(insertMessage, false, information.getOther().getMessage());
                    Database.INSTANCE.manipulateQuery(insertEnabled, false, information.getOther().isEnabled());
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }
        });
        return Response.status(200).build();
    }

}
