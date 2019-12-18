package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import no.capraconsulting.auth.AdminFilter;
import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.domain.Role;
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
    public Response changeUserRole(@PathParam("userId") String userId, String payload) throws SQLException {
        Role role = gson.fromJson(payload, Role.class);
        AdminRepository.changeUserRole(userId, VolunteerRole.valueOf(role.role));
        String userRole = AdminRepository.getUserRole(userId).toString();
        return Response.ok(userRole).build();
    }

    @GET
    @Path("/volunteerrole/{userId}")
    public Response getUserRole(@PathParam("userId") String userId) throws SQLException {
        return Response.ok(AdminRepository.getUserRole(userId).toString()).build();
    }

}
