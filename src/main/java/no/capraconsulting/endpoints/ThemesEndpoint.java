package no.capraconsulting.endpoints;

import no.capraconsulting.auth.JwtFilter;
import no.capraconsulting.db.Database;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

import static no.capraconsulting.endpoints.ThemesEndpoint.THEME_PATH;

@Path(THEME_PATH)
@JwtFilter
public class ThemesEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(SubjectEndpoint.class);
    public static final String THEME_PATH = "/themes";

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response postTheme(String payload){
        JSONObject data = new JSONObject(payload);
        String themeTitle = data.getString("themeTitle");

        String query = "INSERT INTO "
            + "THEMES (theme, subject_id) "
            + "VALUES (?, ?)";

        try {
            if (!themeTitle.isEmpty()) {
                Database.INSTANCE.manipulateQuery(query, false, themeTitle, data.getInt("subjectId"));
                return Response.status(200).build();
            }
            return Response.status(422).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteTheme(@PathParam("id") Integer id){
        String query = "DELETE FROM THEMES WHERE id = ?";

        try {
            Database.INSTANCE.manipulateQuery(query, false, id);
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }
}
