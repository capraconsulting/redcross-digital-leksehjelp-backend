package no.capraconsulting.repository;

import no.capraconsulting.db.Database;
import no.capraconsulting.domain.Information;
import no.capraconsulting.domain.VolunteerRole;
import no.capraconsulting.utils.EndpointUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

    public static void updateIsOpen(boolean isOpen) {
        String insertInformation = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$.isOpen', ?)";

        try {
            Database.INSTANCE.manipulateQuery(insertInformation, false, isOpen);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new InternalServerErrorException("Failed to execute query ", e);
        }
    }

    public static void updateAnnouncement(String announcement) {
        String insertInformation = "UPDATE INFORMATION SET data=JSON_MODIFY(data, '$.announcement', ?)";

        try {
            Database.INSTANCE.manipulateQuery(insertInformation, false, announcement);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new InternalServerErrorException("Failed to execute query ", e);
        }
    }

    public static void updateOpeningHours(Information information) {
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
                throw new InternalServerErrorException("Failed to execute query ", e);
            }
        });
    }
}
