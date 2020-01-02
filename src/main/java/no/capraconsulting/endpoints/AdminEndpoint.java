package no.capraconsulting.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import no.capraconsulting.auth.AdminFilter;
import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.client.MSGraphClient;
import no.capraconsulting.domain.Information;
import no.capraconsulting.domain.Role;
import no.capraconsulting.domain.Subject;
import no.capraconsulting.domain.Theme;
import no.capraconsulting.domain.Volunteer;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.repository.AdminRepository;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.capraconsulting.utils.EndpointUtils.parseMSGraphPayload;


@Path("/admin")
@AdminFilter
@JwtFilter
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

    @POST
    @Path("/subjects/")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addSubject(String payload) {
        Subject subject = gson.fromJson(payload, Subject.class);
        AdminRepository.addSubject(subject.subjectTitle, subject.isMestring);
        return Response.ok().build();
    }

    @DELETE
    @Path("/subjects/{id}")
    @JwtFilter
    public Response deleteSubject(@PathParam("id") Integer id) {
        AdminRepository.deleteSubject(id);
        return Response.ok().build();
    }

    @POST
    @Path("/themes/")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addTheme(String payload){
        Theme theme = gson.fromJson(payload, Theme.class);
        AdminRepository.addTheme(theme.themeTitle, theme.subjectId);
        return Response.ok().build();
    }

    @DELETE
    @Path("/themes/{id}")
    public Response deleteTheme(@PathParam("id") Integer id){
        AdminRepository.deleteTheme(id);
        return Response.ok().build();
    }

    @PUT
    @Path("/information/announcement")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateAnnouncement(String payload) {
        JSONObject data = new JSONObject(payload);
        AdminRepository.updateAnnouncement(data.getString("announcement"));
        return Response.ok().build();
    }

    @PUT
    @Path("/information/open")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateIsOpen(String payload) {
        JSONObject data = new JSONObject(payload);
        AdminRepository.updateIsOpen(data.getBoolean("isOpen"));
        return Response.ok(data.toString()).build();
    }

    @PUT
    @Path("/information/openinghours")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateOpeningHours(String payload) {
        Information information = gson.fromJson(payload, Information.class);
        AdminRepository.updateOpeningHours(information);
        return Response.ok().build();
    }
}
