package no.capraconsulting.domain;

import no.capraconsulting.endpoints.VolunteerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class OpeningHour {
    private static Logger LOG = LoggerFactory.getLogger(VolunteerEndpoint.class);
    private String start;
    private String end;
    private boolean enabled;

    public OpeningHour(String start, String end, boolean enabled) {
        this.enabled = enabled;

        if (isValidHours(start, end)) {
            this.start = start;
            this.end = end;
        }
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private boolean isValidHours(String startHour, String endHour) {
        DateTimeFormatter strictTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

        try {
            LocalTime start = LocalTime.parse(startHour, strictTimeFormatter);
            LocalTime end = LocalTime.parse(endHour, strictTimeFormatter);

            return start.isBefore(end);
        } catch (DateTimeParseException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

}
