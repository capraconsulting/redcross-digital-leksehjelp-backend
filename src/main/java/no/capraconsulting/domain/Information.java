package no.capraconsulting.domain;

public class Information {
    private boolean isOpen;
    private String announcement;
    private OpeningHour monday;
    private OpeningHour tuesday;
    private OpeningHour wednesday;
    private OpeningHour thursday;
    private OpeningHour friday;
    private OpeningHour saturday;
    private OpeningHour sunday;
    private OtherOpeningHour other;

    public Information(
        boolean isOpen,
        String announcement,
        OpeningHour monday,
        OpeningHour tuesday,
        OpeningHour wednesday,
        OpeningHour thursday,
        OpeningHour friday,
        OpeningHour saturday,
        OpeningHour sunday,
        OtherOpeningHour other
    ) {
        this.isOpen = isOpen;
        this.announcement = announcement;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.other = other;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public OpeningHour getMonday() {
        return monday;
    }

    public OpeningHour getTuesday() {
        return tuesday;
    }

    public OpeningHour getWednesday() {
        return wednesday;
    }

    public OpeningHour getThursday() {
        return thursday;
    }

    public OpeningHour getFriday() {
        return friday;
    }

    public OpeningHour getSaturday() {
        return saturday;
    }

    public OpeningHour getSunday() {
        return sunday;
    }

    public OtherOpeningHour getOther() {
        return other;
    }

    public OpeningHour getOpeningHourByDay(String day) {
        switch (day) {
            case "monday":
                return getMonday();
            case "tuesday":
                return getTuesday();
            case "wednesday":
                return getWednesday();
            case "thursday":
                return getThursday();
            case "friday":
                return getFriday();
            case "saturday":
                return getSaturday();
            case "sunday":
                return getSunday();
            default:
                return null;
        }
    }
}
