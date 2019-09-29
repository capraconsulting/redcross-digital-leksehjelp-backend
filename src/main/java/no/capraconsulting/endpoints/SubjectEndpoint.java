package no.capraconsulting.endpoints;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.capraconsulting.db.Database;
import no.capraconsulting.auth.JwtFilter;
import javax.sql.RowSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


import static no.capraconsulting.endpoints.SubjectEndpoint.SUBJECT_PATH;

@Path(SUBJECT_PATH)
public final class SubjectEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(SubjectEndpoint.class);
    public static final String SUBJECT_PATH = "/subjects";

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSubjects(@QueryParam("isMestring") int isMestring) {
        String query = "" +
            "SELECT Subjects.subject, Subjects.id, Themes.id as tid, Themes.theme FROM SUBJECTS " +
            "LEFT JOIN Themes ON SUBJECTS.id = THEMES.subject_id " +
            "WHERE is_mestring = ?";

        try {
            RowSet result  = Database.INSTANCE.selectQuery(query, isMestring);
            Map<Integer, JSONObject> subjects = new HashMap<Integer, JSONObject>();

            while (result.next()) {
                int subjectID = result.getInt("id");

                JSONObject subject = subjects.getOrDefault(subjectID, new JSONObject());
                JSONArray themes = (subject.has("themes")) ?
                    (JSONArray) subject.get("themes") : new JSONArray();

                subject.put("id", result.getInt("id"));
                subject.put("subjectTitle", result.getString("subject"));

                JSONObject theme = new JSONObject();
                int themeID = result.getInt("tid");
                theme.put("id", themeID);
                theme.put("theme", result.getString("theme"));
                if (themeID != 0) themes.put(theme);

                subject.put("themes", themes);
                subjects.put(subjectID, subject);
            }

            return Response.ok(subjects.values().toString()).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }

    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @JwtFilter
    public Response postSubject(String payload){

        JSONObject data = new JSONObject(payload);

        String query = "INSERT INTO "
                      + "SUBJECTS (subject, is_mestring) "
                      + "VALUES (?, ?)";

        try {
            int isMestring;
            if (!data.has("isMestring")) {
                isMestring = 0;
            } else {
                isMestring = data.getInt("isMestring");
            }
            Database.INSTANCE.manipulateQuery(query, false, data.getString("subjectTitle"), isMestring);
            return Response.status(200).build();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            return Response.status(422).build();
        }
    }
}
