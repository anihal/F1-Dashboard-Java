package com.f1dashboard.model;

public class ConstructorStats {
    public final int constructorId;
    public final String name, nationality;
    public final int totalRaces, wins, podiums, championships;
    public final double totalPoints;
    public final int firstSeason, lastSeason;

    public ConstructorStats(int constructorId, String name, String nationality,
                            int totalRaces, int wins, int podiums,
                            int championships, double totalPoints,
                            int firstSeason, int lastSeason) {
        this.constructorId = constructorId;
        this.name = name;
        this.nationality = nationality;
        this.totalRaces = totalRaces;
        this.wins = wins;
        this.podiums = podiums;
        this.championships = championships;
        this.totalPoints = totalPoints;
        this.firstSeason = firstSeason;
        this.lastSeason = lastSeason;
    }

    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public int getTotalRaces() { return totalRaces; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public int getChampionships() { return championships; }
    public double getTotalPoints() { return totalPoints; }
    public int getFirstSeason() { return firstSeason; }
    public int getLastSeason() { return lastSeason; }
}
