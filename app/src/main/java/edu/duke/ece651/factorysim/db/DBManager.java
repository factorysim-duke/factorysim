package edu.duke.ece651.factorysim.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the SQLite database connection.
 */
public class DBManager {
    // private static final String DB_URL = "jdbc:sqlite:data/factory.db";
    /**
     * Connects to the SQLite database.
     *
     * @return Connection object or null if connection fails.
     */
    public static Connection connect() {
        try {
            File dbDir = new File("data");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            String dbPath = "jdbc:sqlite:data/factory.db";
            return DriverManager.getConnection(dbPath);
        } catch (SQLException e) {
            System.err.println("connect fail" + e.getMessage());
            return null;
        }
    }

}
