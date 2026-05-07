package com.f1dashboard.db;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private static final String TEST_DB = "test_f1.db";
    private DatabaseManager manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = new DatabaseManager(TEST_DB);
        manager.initializeSchema();
    }

    @AfterEach
    void tearDown() throws Exception {
        manager.close();
        Files.deleteIfExists(Path.of(TEST_DB));
    }

    @Test
    void schemaCreatesAllRequiredTables() throws Exception {
        Connection conn = manager.getConnection();
        DatabaseMetaData meta = conn.getMetaData();
        for (String table : new String[]{"drivers", "constructors", "races",
                "results", "driver_standings", "constructor_standings", "status"}) {
            ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"});
            assertTrue(rs.next(), "Missing table: " + table);
        }
    }

    @Test
    void isPopulatedReturnsFalseOnEmptyDatabase() throws Exception {
        assertFalse(manager.isPopulated());
    }
}
