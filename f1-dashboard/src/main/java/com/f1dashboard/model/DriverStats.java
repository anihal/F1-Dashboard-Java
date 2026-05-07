package com.f1dashboard.model;

public class DriverStats {
    public final int driverId;
    public final String fullName;
    public final int totalRaces, wins, podiums, polePositions, championships;
    public final double totalPoints;
    public final int firstSeason, lastSeason;

    public DriverStats(int driverId, String fullName, int totalRaces, int wins,
                       int podiums, int polePositions, int championships,
                       double totalPoints, int firstSeason, int lastSeason) {
        this.driverId = driverId;
        this.fullName = fullName;
        this.totalRaces = totalRaces;
        this.wins = wins;
        this.podiums = podiums;
        this.polePositions = polePositions;
        this.championships = championships;
        this.totalPoints = totalPoints;
        this.firstSeason = firstSeason;
        this.lastSeason = lastSeason;
    }

    public String getFullName() { return fullName; }
    public int getTotalRaces() { return totalRaces; }
    public int getWins() { return wins; }
    public int getPodiums() { return podiums; }
    public int getPolePositions() { return polePositions; }
    public int getChampionships() { return championships; }
    public double getTotalPoints() { return totalPoints; }
    public int getFirstSeason() { return firstSeason; }
    public int getLastSeason() { return lastSeason; }
}
