package no.capraconsulting.repository;

import no.capraconsulting.db.Database;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.utils.EndpointUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import java.sql.SQLException;

public class AdminRepository {
    private static Logger LOG = LoggerFactory.getLogger(AdminRepository.class);

    public static JSONArray getUserRole(String volunteer_id)  {
        String query = "SELECT ROLE AS 'role' FROM VOLUNTEERS WHERE id = ?";
        try {
            return EndpointUtils.buildPayload(Database.INSTANCE.selectQuery(query, volunteer_id));
        } catch (SQLException e ) {
            LOG.error(e.getMessage());
            throw new InternalServerErrorException("Failed to execute query: ", e);
        }
    }

    public static void changeUserRole(String volunteer_id, VolunteerRole role) {
        String query = "UPDATE VOLUNTEERS SET ROLE = ? WHERE id = ? ";
        try {
            Database.INSTANCE.manipulateQuery(query, false, role.toString(), volunteer_id);
        } catch (SQLException e ) {
            LOG.error(e.getMessage());
            throw new InternalServerErrorException("Failed to execute query: ", e);
        }
    }

    public static void addVolunteer(String id, String name, String email, VolunteerRole role) {
        String query = "INSERT INTO VOLUNTEERS VALUES (?, ?, ?, ?, ?, ?);";
        try {
            Database.INSTANCE.manipulateQuery(query, false, id, name, "", "", email, role.toString());
        } catch (SQLException e ) {
           LOG.error(e.getMessage());
           throw new InternalServerErrorException("Failed to execute query: ", e);
        }
    }

    public static void deleteVolunteer(String id) {
        String query = "DELETE FROM VOLUNTEERS WHERE id = ?";
        try {
            Database.INSTANCE.manipulateQuery(query, false, id);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new InternalServerErrorException("Failed to execute query ", e);
        }
    }
}
