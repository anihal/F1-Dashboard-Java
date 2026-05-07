package com.f1dashboard.dao;

import com.f1dashboard.model.Driver;
import com.f1dashboard.model.DriverStats;

import java.sql.*;
import java.util.*;

public class DriverDao {

    private final Connection conn;

    public DriverDao(Connection conn) {
        this.conn = conn;
    }

    public List<DriverStats> getTopDriversByWins(int limit, Integer year) throws SQLException {
        List<DriverStats> list = new ArrayList<>();
        String yearFilter = (year != null) ? " AND rc.year = " + year : "";
        String sql = """
            SELECT d.driverId, d.forename || ' ' || d.surname AS name,
                   COUNT(*) AS races,
                   SUM(CASE WHEN r.positionOrder=1 THEN 1 ELSE 0 END) AS wins,
                   SUM(CASE WHEN r.positionOrder<=3 THEN 1 ELSE 0 END) AS podiums,
                   SUM(CASE WHEN r.grid=1 THEN 1 ELSE 0 END) AS poles,
                   SUM(r.points) AS pts,
                   MIN(rc.year) AS fy, MAX(rc.year) AS ly,
                   (SELECT COUNT(*) FROM (
                       SELECT ra.year, MAX(ra.raceId) AS lastRace
                       FROM races ra GROUP BY ra.year
                   ) seasons
                   JOIN driver_standings ds ON ds.raceId = seasons.lastRace
                   WHERE ds.driverId = d.driverId AND ds.position = 1) AS championships
            FROM drivers d
            JOIN results r ON d.driverId = r.driverId
            JOIN races rc ON r.raceId = rc.raceId
            WHERE 1=1 """ + yearFilter + """

            GROUP BY d.driverId
            ORDER BY wins DESC
            LIMIT ?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DriverStats(
                    rs.getInt("driverId"), rs.getString("name"),
                    rs.getInt("races"), rs.getInt("wins"), rs.getInt("podiums"),
                    rs.getInt("poles"), rs.getInt("championships"), rs.getDouble("pts"),
                    rs.getInt("fy"), rs.getInt("ly")));
            }
        }
        return list;
    }

    public int getChampionships(int driverId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT ra.year, MAX(ra.raceId) AS lastRace
                FROM races ra GROUP BY ra.year
            ) seasons
            JOIN driver_standings ds ON ds.raceId = seasons.lastRace
            WHERE ds.driverId = ? AND ds.position = 1""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Driver> searchDrivers(String query) throws SQLException {
        List<Driver> list = new ArrayList<>();
        String sql = """
            SELECT driverId, forename, surname, nationality, dob, code FROM drivers
            WHERE LOWER(forename || ' ' || surname) LIKE LOWER(?)
            ORDER BY surname""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Driver(
                    rs.getInt("driverId"), rs.getString("forename"),
                    rs.getString("surname"), rs.getString("nationality"),
                    rs.getString("dob"), rs.getString("code")));
            }
        }
        return list;
    }

    public DriverStats getDriverStats(int driverId, Integer year) throws SQLException {
        String nameSql = "SELECT forename || ' ' || surname FROM drivers WHERE driverId = ?";
        String fullName;
        try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            fullName = rs.next() ? rs.getString(1) : "Unknown";
        }
        String yearFilter = (year != null) ? " AND rc.year = " + year : "";
        String sql = """
            SELECT COUNT(*) AS races,
                   SUM(CASE WHEN positionOrder=1 THEN 1 ELSE 0 END) AS wins,
                   SUM(CASE WHEN positionOrder<=3 THEN 1 ELSE 0 END) AS podiums,
                   SUM(CASE WHEN grid=1 THEN 1 ELSE 0 END) AS poles,
                   SUM(r.points) AS pts,
                   MIN(rc.year) AS fy, MAX(rc.year) AS ly
            FROM results r JOIN races rc ON r.raceId = rc.raceId
            WHERE r.driverId = ? """ + yearFilter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            int championships = getChampionships(driverId);
            return new DriverStats(driverId, fullName, rs.getInt("races"),
                rs.getInt("wins"), rs.getInt("podiums"), rs.getInt("poles"),
                championships, rs.getDouble("pts"), rs.getInt("fy"), rs.getInt("ly"));
        }
    }

    public Map<Integer, Double> getPointsPerSeason(int driverId) throws SQLException {
        Map<Integer, Double> map = new TreeMap<>();
        String sql = """
            SELECT rc.year, SUM(r.points) AS pts
            FROM results r JOIN races rc ON r.raceId = rc.raceId
            WHERE r.driverId = ?
            GROUP BY rc.year ORDER BY rc.year""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getInt(1), rs.getDouble(2));
        }
        return map;
    }

    public String getNationality(int driverId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nationality FROM drivers WHERE driverId=?")) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        }
    }

    public String getDob(int driverId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT dob FROM drivers WHERE driverId=?")) {
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        }
    }

    public List<Integer> getAvailableYears() throws SQLException {
        List<Integer> years = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT DISTINCT year FROM races ORDER BY year DESC")) {
            while (rs.next()) years.add(rs.getInt(1));
        }
        return years;
    }
}
