package edu.duke.ece651.factorysim.db;

import java.sql.*;

/**
 * Manages session data in the database.
 */
public class SessionDAO {

    /**
     * Adds or updates a session in the database.
     */
    public static void saveSession(String userId, String gameStateJson) {
        String sql = "REPLACE INTO sessions(user_id, game_state) VALUES(?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, gameStateJson);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a session from the database.
     *
     * @param userId The user ID of the session to load.
     * @return The game state JSON string, or null if not found.
     */
    public static String loadSession(String userId) {
        String sql = "SELECT game_state FROM sessions WHERE user_id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("game_state");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a session from the database.
     *
     * @param userId The user ID of the session to delete.
     */
    public static void deleteSession(String userId) {
        String sql = "DELETE FROM sessions WHERE user_id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
