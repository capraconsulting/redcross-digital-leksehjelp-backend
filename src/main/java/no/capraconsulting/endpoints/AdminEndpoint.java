package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.capraconsulting.auth.AdminFilter;
import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.client.MSGraphClient;
import no.capraconsulting.domain.Role;
import no.capraconsulting.domain.Volunteer;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.repository.AdminRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.capraconsulting.utils.EndpointUtils.parseMSGraphPayload;


@Path("/admin")
public class AdminEndpoint {
    private static Gson gson = new Gson();
    private MSGraphClient client = new MSGraphClient();

    @POST
    @Path("/volunteerrole/{userId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response changeUserRole(@PathParam("userId") String userId, String payload) {
        Role role = gson.fromJson(payload, Role.class);
        AdminRepository.changeUserRole(userId, VolunteerRole.valueOf(role.role));
        String userRole = AdminRepository.getUserRole(userId).toString();
        return Response.ok(userRole).build();
    }

    @GET
    @Path("/volunteerrole/{userId}")
    public Response getUserRole(@PathParam("userId") String userId) {
        return Response.ok(AdminRepository.getUserRole(userId).toString()).build();
    }


    @POST
    @Path("/volunteer/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addVolunteer(String payload) {
        Volunteer volunteer = gson.fromJson(payload, Volunteer.class);
        JsonObject apiPayload = client.getUserIdByEmail(volunteer.email);
        String volunteerId = parseMSGraphPayload(apiPayload);
        AdminRepository.addVolunteer(volunteerId, volunteer.name, volunteer.email, volunteer.role);
        return Response.ok().build();
    }

    @DELETE
    @Path("/volunteer/{userId}")
    public Response deleteVolunteer(@PathParam("userId") String userId) {
        AdminRepository.deleteVolunteer(userId);
       return Response.ok().build();
    }
}
