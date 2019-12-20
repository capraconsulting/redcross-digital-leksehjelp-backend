package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import no.capraconsulting.auth.AdminFilter;
import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.domain.Role;
import no.capraconsulting.domain.Volunteer;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.repository.AdminRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;


@Path("/admin")
@AdminFilter
@JwtFilter
public class AdminEndpoint {
    private static Gson gson = new Gson();

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
        String id = ""; //get id from Azure Active Directory
        AdminRepository.addVolunteer(id, volunteer.name, volunteer.email, volunteer.role);
        return Response.ok().build();
    }

    @DELETE
    @Path("/volunteer/{userId}")
    public Response deleteVolunteer(@PathParam("userId") String userId) {
        AdminRepository.deleteVolunteer(userId);
       return Response.ok().build();
    }
}
