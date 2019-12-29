package no.capraconsulting.domain;

public class Volunteer {

    public String name;
    public String email;
    public VolunteerRole role;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public VolunteerRole getRole() {
        return role;
    }

    public void setRole(VolunteerRole role) { this.role = role; }

}
