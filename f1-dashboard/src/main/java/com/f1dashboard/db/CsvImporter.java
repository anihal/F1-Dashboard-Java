package com.f1dashboard.db;

import javafx.concurrent.Task;
import java.io.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;

public class CsvImporter extends Task<Void> {

    private final String dataDir;
    private final DatabaseManager db;

    public CsvImporter(String dataDir, DatabaseManager db) {
        this.dataDir = dataDir;
        this.db = db;
    }

    @Override
    protected Void call() throws Exception {
        Connection conn = db.getConnection();
        conn.setAutoCommit(false);
        try {
            updateMessage("Importing status...");
            updateProgress(0, 7);
            importStatus(conn);

            updateMessage("Importing drivers...");
            updateProgress(1, 7);
            importDrivers(conn);

            updateMessage("Importing constructors...");
            updateProgress(2, 7);
            importConstructors(conn);

            updateMessage("Importing races...");
            updateProgress(3, 7);
            importRaces(conn);

            updateMessage("Importing results...");
            updateProgress(4, 7);
            importResults(conn);

            updateMessage("Importing driver standings...");
            updateProgress(5, 7);
            importDriverStandings(conn);

            updateMessage("Importing constructor standings...");
            updateProgress(6, 7);
            importConstructorStandings(conn);

            conn.commit();
            updateMessage("Import complete.");
            updateProgress(7, 7);
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            updateMessage("Import failed: " + e.getMessage());
            throw e;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
        }
        return null;
    }

    private String nullify(String val) {
        return (val == null || val.equals("\\N") || val.isBlank()) ? null : val;
    }

    private BufferedReader readerFor(String filename) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(dataDir + "/" + filename), StandardCharsets.UTF_8));
    }

    private void importStatus(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO status(statusId, status) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("status.csv")) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setString(2, nullify(p[1]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importDrivers(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO drivers VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("drivers.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setString(2, nullify(p[1]));
                ps.setString(3, nullify(p[2]));
                ps.setString(4, nullify(p[3]));
                ps.setString(5, nullify(p[4]));
                ps.setString(6, nullify(p[5]));
                ps.setString(7, nullify(p[6]));
                ps.setString(8, nullify(p[7]));
                ps.setString(9, nullify(p[8]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importConstructors(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO constructors VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("constructors.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                for (int i = 1; i <= 4; i++) ps.setString(i + 1, nullify(p[i]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importRaces(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO races(raceId,year,round,circuitId,name,date,time,url) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("races.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setInt(2, Integer.parseInt(p[1]));
                ps.setInt(3, Integer.parseInt(p[2]));
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setString(5, nullify(p[4]));
                ps.setString(6, nullify(p[5]));
                ps.setString(7, nullify(p[6]));
                ps.setString(8, nullify(p[7]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importResults(Connection conn) throws Exception {
        String sql = """
            INSERT OR IGNORE INTO results
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("results.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setInt(2, Integer.parseInt(p[1]));
                ps.setInt(3, Integer.parseInt(p[2]));
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setString(5, nullify(p[4]));
                ps.setObject(6, nullify(p[5]) == null ? null : Integer.parseInt(p[5]));
                ps.setString(7, nullify(p[6]));
                ps.setString(8, nullify(p[7]));
                ps.setObject(9, nullify(p[8]) == null ? null : Integer.parseInt(p[8]));
                ps.setObject(10, nullify(p[9]) == null ? null : Double.parseDouble(p[9]));
                ps.setObject(11, nullify(p[10]) == null ? null : Integer.parseInt(p[10]));
                ps.setString(12, nullify(p[11]));
                ps.setString(13, nullify(p[12]));
                ps.setString(14, nullify(p[13]));
                ps.setString(15, nullify(p[14]));
                ps.setString(16, nullify(p[15]));
                ps.setString(17, nullify(p[16]));
                ps.setObject(18, nullify(p[17]) == null ? null : Integer.parseInt(p[17]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importDriverStandings(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO driver_standings VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("driver_standings.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setInt(2, Integer.parseInt(p[1]));
                ps.setInt(3, Integer.parseInt(p[2]));
                ps.setObject(4, nullify(p[3]) == null ? null : Double.parseDouble(p[3]));
                ps.setObject(5, nullify(p[4]) == null ? null : Integer.parseInt(p[4]));
                ps.setString(6, nullify(p[5]));
                ps.setObject(7, nullify(p[6]) == null ? null : Integer.parseInt(p[6]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void importConstructorStandings(Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO constructor_standings VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = readerFor("constructor_standings.csv")) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = parseCsvLine(line);
                ps.setInt(1, Integer.parseInt(p[0]));
                ps.setInt(2, Integer.parseInt(p[1]));
                ps.setInt(3, Integer.parseInt(p[2]));
                ps.setObject(4, nullify(p[3]) == null ? null : Double.parseDouble(p[3]));
                ps.setObject(5, nullify(p[4]) == null ? null : Integer.parseInt(p[4]));
                ps.setString(6, nullify(p[5]));
                ps.setObject(7, nullify(p[6]) == null ? null : Integer.parseInt(p[6]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Parses a single CSV line handling quoted fields (fields may contain commas).
     */
    static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // skip the second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}
