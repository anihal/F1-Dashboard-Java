package com.f1dashboard.dao;

import com.f1dashboard.model.Race;
import com.f1dashboard.model.RaceResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RaceDao {

    private final Connection conn;

    public RaceDao(Connection conn) {
        this.conn = conn;
    }

    public List<Race> getRacesByYear(int year) throws SQLException {
        List<Race> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT raceId, year, round, name, date FROM races WHERE year = ? ORDER BY round")) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Race(rs.getInt("raceId"), rs.getInt("year"),
                    rs.getInt("round"), rs.getString("name"), rs.getString("date")));
            }
        }
        return list;
    }

    public List<RaceResult> getResultsForRace(int raceId) throws SQLException {
        List<RaceResult> list = new ArrayList<>();
        String sql = """
            SELECT r.positionOrder, r.positionText,
                   d.forename || ' ' || d.surname AS driver,
                   c.name AS constructor,
                   r.laps, r.points,
                   COALESCE(s.status, 'Unknown') AS status
            FROM results r
            JOIN drivers d ON r.driverId = d.driverId
            JOIN constructors c ON r.constructorId = c.constructorId
            LEFT JOIN status s ON r.statusId = s.statusId
            WHERE r.raceId = ?
            ORDER BY r.positionOrder""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, raceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new RaceResult(
                    rs.getInt("positionOrder"), rs.getString("positionText"),
                    rs.getString("driver"), rs.getString("constructor"),
                    rs.getInt("laps"), rs.getDouble("points"), rs.getString("status")));
            }
        }
        return list;
    }

    public List<Integer> getAvailableYears() throws SQLException {
        List<Integer> years = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT DISTINCT year FROM races ORDER BY year DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) years.add(rs.getInt(1));
        }
        return years;
    }
}
