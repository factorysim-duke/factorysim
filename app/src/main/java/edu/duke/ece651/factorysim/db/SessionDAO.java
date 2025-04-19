package edu.duke.ece651.factorysim.db;

import java.sql.*;

public class SessionDAO {

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
