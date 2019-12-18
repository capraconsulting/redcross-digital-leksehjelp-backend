package no.capraconsulting.repository;

import no.capraconsulting.db.Database;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.utils.EndpointUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AdminRepository {
    private static Logger LOG = LoggerFactory.getLogger(AdminRepository.class);

    public static JSONArray getUserRole(String volunteer_id) throws SQLException  {
        String query = "SELECT ROLE AS 'role' FROM VOLUNTEERS WHERE id = ?";
        try {
            return EndpointUtils.buildPayload(Database.INSTANCE.selectQuery(query, volunteer_id));
        } catch (SQLException e ) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    public static void changeUserRole(String volunteer_id, VolunteerRole role) throws SQLException {
        String query = "UPDATE VOLUNTEERS SET ROLE = ? WHERE id = ? ";
        Database.INSTANCE.manipulateQuery(query, false, role.toString(), volunteer_id);
    }
}
