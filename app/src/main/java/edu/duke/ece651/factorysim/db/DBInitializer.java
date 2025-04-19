package edu.duke.ece651.factorysim.db;

import java.sql.Connection;
import java.sql.Statement;

public class DBInitializer {
    /**
     * Initializes the database.
     */
    public static void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS sessions (
                user_id TEXT PRIMARY KEY,
                game_state TEXT NOT NULL
            );
        """;

        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
//            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
