package edu.duke.ece651.factorysim.db;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class DBIntegrationTest {

    private static final File DB_DIR  = new File("data");
    private static final File DB_FILE = new File("data/factory.db");

    private static void deleteRecursively(File f) throws IOException {
        if (!f.exists()) return;
        Files.walk(f.toPath())
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        // Delete old data/ directory and its contents
        deleteRecursively(DB_DIR);
        assertFalse(DB_DIR.exists(), "precondition: data/ must not exist");

        // Normal init() call, which will create data/ directory, empty factory.db, and sessions table
        DBInitializer.init();
        assertTrue(DB_DIR.isDirectory(), "data/ directory should now exist");
        assertTrue(DB_FILE.isFile(), "factory.db should now exist");
    }

    @AfterAll
    static void afterAll() throws IOException {
        // Clean up after all tests
        deleteRecursively(DB_DIR);
    }


    @BeforeEach
    void beforeEach() throws SQLException {
        // Ensure sessions table exists and clear it
        DBInitializer.init();
        try (Connection conn = DBManager.connect();
             Statement  st   = conn.createStatement()) {
            st.execute("DELETE FROM sessions");
        }
    }


    @Test
    @DisplayName("DBManager.connect() returns a valid, open Connection")
    void testConnectNormal() throws SQLException {
        Connection conn = DBManager.connect();
        assertNotNull(conn, "connect() should not return null");
        assertFalse(conn.isClosed(), "Connection should be open");
        conn.close();
    }

    @Test
    @DisplayName("DBManager.connect() reuses existing data/ dir without error")
    void testConnectReusesDir() throws Exception {
        // data/ and factory.db already exist
        long beforeSize = DB_FILE.length();
        try (Connection conn = DBManager.connect()) {
            assertNotNull(conn);
        }
        assertTrue(DB_FILE.length() >= beforeSize,
                   "factory.db should still exist and not be truncated");
    }

    @Test
    @DisplayName("DBManager.connect() returns null on SQLException (data is file)")
    void testConnectSQLExceptionBranch() throws Exception {
        // Delete data/ directory and create a regular file with the same name
        deleteRecursively(DB_DIR);
        assertTrue(DB_DIR.createNewFile(), "setup: data as a file");
        Connection conn = DBManager.connect();
        assertNull(conn, "connect() should return null when it cannot open DB");
        // cleanup
        assertTrue(DB_DIR.delete(), "cleanup data-file");
    }


    @Test
    @DisplayName("DBInitializer.init() creates sessions table in normal case")
    void testInitCreatesTable() throws SQLException {
        try (Connection conn = DBManager.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name FROM sqlite_master WHERE type='table' AND name='sessions'"
             );
             ResultSet rs = ps.executeQuery()
        ) {
            assertTrue(rs.next(), "sessions table should exist after init()");
        }
    }

    @Test
    @DisplayName("DBInitializer.init() catch branch when data is a file")
    void testInitCatchBranch() throws IOException {
        // Delete data/ and create a regular file
        deleteRecursively(DB_DIR);
        assertTrue(DB_DIR.createNewFile(), "setup: data as a file");

        // init() should not throw exception and won't create sessions table
        assertDoesNotThrow(DBInitializer::init);

        // sessions table shouldn't exist; factory.db cannot be generated
        assertFalse(DB_FILE.exists(), "factory.db should not exist after failed init()");
        assertTrue(DB_DIR.isFile(), "data should still be a plain file");

        // cleanup
        assertTrue(DB_DIR.delete(), "cleanup data-file");
    }


    @Test
    @DisplayName("saveSession + loadSession normal flow")
    void testSaveAndLoadSession() {
        String uid = "userA", state = "{\"lvl\":1}";
        SessionDAO.saveSession(uid, state);
        assertEquals(state, SessionDAO.loadSession(uid));
    }

    @Test
    @DisplayName("saveSession twice updates existing row")
    void testSaveSessionUpdatesExisting() {
        String uid = "userB";
        SessionDAO.saveSession(uid, "old");
        SessionDAO.saveSession(uid, "new");
        assertEquals("new", SessionDAO.loadSession(uid));
    }

    @Test
    @DisplayName("loadSession returns null for missing user")
    void testLoadNonExisting() {
        assertNull(SessionDAO.loadSession("no-such"));
    }

    @Test
    @DisplayName("deleteSession removes the row")
    void testDeleteSession() {
        String uid = "userC";
        SessionDAO.saveSession(uid, "xxx");
        SessionDAO.deleteSession(uid);
        assertNull(SessionDAO.loadSession(uid));
    }

    @Test
    @DisplayName("saveSession catch-SQLException when sessions table missing")
    void testSaveSessionSQLException() throws SQLException {
        // Force drop table
        try (Connection conn = DBManager.connect();
             Statement  st   = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS sessions");
        }
        assertDoesNotThrow(() -> SessionDAO.saveSession("x", "y"));
    }

    @Test
    @DisplayName("loadSession catch-SQLException when sessions table missing")
    void testLoadSessionSQLException() throws SQLException {
        try (Connection conn = DBManager.connect();
             Statement  st   = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS sessions");
        }
        assertNull(SessionDAO.loadSession("x"));
    }

    @Test
    @DisplayName("deleteSession catch-SQLException when sessions table missing")
    void testDeleteSessionSQLException() throws SQLException {
        try (Connection conn = DBManager.connect();
             Statement  st   = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS sessions");
        }
        assertDoesNotThrow(() -> SessionDAO.deleteSession("x"));
    }
}
