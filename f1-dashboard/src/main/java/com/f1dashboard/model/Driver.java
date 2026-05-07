package com.f1dashboard.model;

public class Driver {
    public final int driverId;
    public final String forename, surname, nationality, dob, code;

    public Driver(int driverId, String forename, String surname,
                  String nationality, String dob, String code) {
        this.driverId = driverId;
        this.forename = forename;
        this.surname = surname;
        this.nationality = nationality;
        this.dob = dob;
        this.code = code;
    }

    public String fullName() { return forename + " " + surname; }
}
