package com.f1dashboard.model;

public class RaceResult {
    public final int positionOrder;
    public final String positionText, driverName, constructorName, statusText;
    public final int laps;
    public final double points;

    public RaceResult(int positionOrder, String positionText, String driverName,
                      String constructorName, int laps, double points, String statusText) {
        this.positionOrder = positionOrder;
        this.positionText = positionText;
        this.driverName = driverName;
        this.constructorName = constructorName;
        this.laps = laps;
        this.points = points;
        this.statusText = statusText;
    }

    public String getPositionText() { return positionText; }
    public String getDriverName() { return driverName; }
    public String getConstructorName() { return constructorName; }
    public int getLaps() { return laps; }
    public double getPoints() { return points; }
    public String getStatusText() { return statusText; }
}
