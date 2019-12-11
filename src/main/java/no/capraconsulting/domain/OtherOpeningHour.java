package no.capraconsulting.domain;

public class OtherOpeningHour {
    private String message;
    private boolean enabled;

    public OtherOpeningHour(String message, boolean enabled) {
        this.enabled = enabled;

        if (enabled && message.length() > 0) {
            this.message = message;
        }
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
