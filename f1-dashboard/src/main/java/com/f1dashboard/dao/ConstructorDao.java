package com.f1dashboard.dao;

import com.f1dashboard.model.Constructor;
import com.f1dashboard.model.ConstructorStats;

import java.sql.*;
import java.util.*;

public class ConstructorDao {

    private final Connection conn;

    public ConstructorDao(Connection conn) {
        this.conn = conn;
    }

    public List<ConstructorStats> getTopConstructorsByWins(int limit, Integer year) throws SQLException {
        List<ConstructorStats> list = new ArrayList<>();
        String yearFilter = (year != null) ? " AND rc.year = " + year : "";
        String sql = """
            SELECT c.constructorId, c.name, c.nationality,
                   COUNT(DISTINCT r.raceId) AS races,
                   SUM(CASE WHEN r.positionOrder=1 THEN 1 ELSE 0 END) AS wins,
                   SUM(CASE WHEN r.positionOrder<=3 THEN 1 ELSE 0 END) AS podiums,
                   SUM(r.points) AS pts,
                   MIN(rc.year) AS fy, MAX(rc.year) AS ly,
                   (SELECT COUNT(*) FROM (
                       SELECT ra.year, MAX(ra.raceId) AS lastRace
                       FROM races ra GROUP BY ra.year
                   ) seasons
                   JOIN constructor_standings cs ON cs.raceId = seasons.lastRace
                   WHERE cs.constructorId = c.constructorId AND cs.position = 1) AS championships
            FROM constructors c
            JOIN results r ON c.constructorId = r.constructorId
            JOIN races rc ON r.raceId = rc.raceId
            WHERE 1=1 """ + yearFilter + """

            GROUP BY c.constructorId ORDER BY wins DESC LIMIT ?""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ConstructorStats(
                    rs.getInt("constructorId"), rs.getString("name"),
                    rs.getString("nationality"), rs.getInt("races"),
                    rs.getInt("wins"), rs.getInt("podiums"), rs.getInt("championships"),
                    rs.getDouble("pts"), rs.getInt("fy"), rs.getInt("ly")));
            }
        }
        return list;
    }

    public int getChampionships(int constructorId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT ra.year, MAX(ra.raceId) AS lastRace
                FROM races ra GROUP BY ra.year
            ) seasons
            JOIN constructor_standings cs ON cs.raceId = seasons.lastRace
            WHERE cs.constructorId = ? AND cs.position = 1""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, constructorId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Constructor> searchConstructors(String query) throws SQLException {
        List<Constructor> list = new ArrayList<>();
        String sql = """
            SELECT constructorId, name, nationality FROM constructors
            WHERE LOWER(name) LIKE LOWER(?) ORDER BY name""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Constructor(rs.getInt("constructorId"),
                    rs.getString("name"), rs.getString("nationality")));
            }
        }
        return list;
    }

    public ConstructorStats getConstructorStats(int constructorId, Integer year) throws SQLException {
        String yearFilter = (year != null) ? " AND rc.year = " + year : "";
        String sql = """
            SELECT c.name, c.nationality,
                   COUNT(DISTINCT r.raceId) AS races,
                   SUM(CASE WHEN r.positionOrder=1 THEN 1 ELSE 0 END) AS wins,
                   SUM(CASE WHEN r.positionOrder<=3 THEN 1 ELSE 0 END) AS podiums,
                   SUM(r.points) AS pts,
                   MIN(rc.year) AS fy, MAX(rc.year) AS ly
            FROM constructors c
            JOIN results r ON c.constructorId = r.constructorId
            JOIN races rc ON r.raceId = rc.raceId
            WHERE c.constructorId = ? """ + yearFilter;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, constructorId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            int championships = getChampionships(constructorId);
            return new ConstructorStats(constructorId, rs.getString("name"),
                rs.getString("nationality"), rs.getInt("races"), rs.getInt("wins"),
                rs.getInt("podiums"), championships, rs.getDouble("pts"),
                rs.getInt("fy"), rs.getInt("ly"));
        }
    }

    public Map<Integer, Integer> getWinsPerSeason(int constructorId) throws SQLException {
        Map<Integer, Integer> map = new TreeMap<>();
        String sql = """
            SELECT rc.year, COUNT(*) AS wins
            FROM results r JOIN races rc ON r.raceId = rc.raceId
            WHERE r.constructorId = ? AND r.positionOrder = 1
            GROUP BY rc.year ORDER BY rc.year""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, constructorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getInt(1), rs.getInt(2));
        }
        return map;
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
