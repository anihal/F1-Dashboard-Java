package com.f1dashboard.model;

public class Race {
    public final int raceId, year, round;
    public final String name, date;

    public Race(int raceId, int year, int round, String name, String date) {
        this.raceId = raceId;
        this.year = year;
        this.round = round;
        this.name = name;
        this.date = date;
    }

    public int getRound() { return round; }
    public String getName() { return name; }
    public String getDate() { return date; }
}
