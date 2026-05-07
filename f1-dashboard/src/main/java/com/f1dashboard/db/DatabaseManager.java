package com.f1dashboard.db;

import java.sql.*;

public class DatabaseManager {

    private final String dbPath;
    private Connection connection;

    public DatabaseManager(String dbPath) throws SQLException {
        this.dbPath = dbPath;
    }

    public void initializeSchema() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement pragmaStmt = connection.createStatement()) {
            pragmaStmt.execute("PRAGMA foreign_keys = ON");
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS status (
                    statusId INTEGER PRIMARY KEY,
                    status TEXT NOT NULL
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS drivers (
                    driverId INTEGER PRIMARY KEY,
                    driverRef TEXT,
                    number TEXT,
                    code TEXT,
                    forename TEXT,
                    surname TEXT,
                    dob TEXT,
                    nationality TEXT,
                    url TEXT
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS constructors (
                    constructorId INTEGER PRIMARY KEY,
                    constructorRef TEXT,
                    name TEXT,
                    nationality TEXT,
                    url TEXT
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS races (
                    raceId INTEGER PRIMARY KEY,
                    year INTEGER,
                    round INTEGER,
                    circuitId INTEGER,
                    name TEXT,
                    date TEXT,
                    time TEXT,
                    url TEXT
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS results (
                    resultId INTEGER PRIMARY KEY,
                    raceId INTEGER REFERENCES races(raceId),
                    driverId INTEGER REFERENCES drivers(driverId),
                    constructorId INTEGER REFERENCES constructors(constructorId),
                    number TEXT,
                    grid INTEGER,
                    position TEXT,
                    positionText TEXT,
                    positionOrder INTEGER,
                    points REAL,
                    laps INTEGER,
                    time TEXT,
                    milliseconds TEXT,
                    fastestLap TEXT,
                    rank TEXT,
                    fastestLapTime TEXT,
                    fastestLapSpeed TEXT,
                    statusId INTEGER REFERENCES status(statusId)
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS driver_standings (
                    driverStandingsId INTEGER PRIMARY KEY,
                    raceId INTEGER REFERENCES races(raceId),
                    driverId INTEGER REFERENCES drivers(driverId),
                    points REAL,
                    position INTEGER,
                    positionText TEXT,
                    wins INTEGER
                )""");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS constructor_standings (
                    constructorStandingsId INTEGER PRIMARY KEY,
                    raceId INTEGER REFERENCES races(raceId),
                    constructorId INTEGER REFERENCES constructors(constructorId),
                    points REAL,
                    position INTEGER,
                    positionText TEXT,
                    wins INTEGER
                )""");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_results_driverId ON results(driverId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_results_constructorId ON results(constructorId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_results_raceId ON results(raceId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_races_year ON races(year)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_driver_standings_driverId ON driver_standings(driverId)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_constructor_standings_constructorId ON constructor_standings(constructorId)");
        }
    }

    public boolean isPopulated() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM drivers")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public Connection getConnection() { return connection; }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
