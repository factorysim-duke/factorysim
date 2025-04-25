package edu.duke.ece651.factorysim;

import edu.duke.ece651.factorysim.client.*;
import java.io.IOException;

public class ServerConnectionManager {
    private static final ServerConnectionManager instance = new ServerConnectionManager();

    public static ServerConnectionManager getInstance() {
        return instance;
    }

    private ServerConnection conn = null;

    private ServerConnectionManager() { }

    public String loadPreset(String host, int port, String presetPath) throws IOException {
        try (ServerConnection conn = new ServerConnection(host, port)) {
            ServerMessage response = conn.loadPreset(presetPath);
            if (!response.status.equalsIgnoreCase("ok")) {
                throw new RuntimeException("Failed to load preset from the server: " + response.message);
            }
            return response.payload;
        }
    }

    public void connect(String host, int port, String user, String password) throws IOException {
        // Try to establish connection with the server
        ServerConnection conn = new ServerConnection(host, port);

        // Try to log in
        ServerMessage response = conn.login(user, password);
        if (!response.status.equalsIgnoreCase("ok")) {
            // Login failed, try to sign up
            response = conn.signup(user, password);
            if (!response.status.equalsIgnoreCase("ok")) {
                throw new RuntimeException("Failed to log into the server: " + response.message);
            }
        }

        // Successfully connected and logged in
        this.conn = conn;
    }

    public void disconnect() {
        if (conn == null) {
            return;
        }
        conn.close();
        conn = null;
    }

    public String loadUserSave() throws IOException {
        if (conn == null) {
            throw new RuntimeException("Failed to load user save: Disconnected");
        }

        ServerMessage response = conn.loadUserSave();
        if (!response.status.equalsIgnoreCase("ok")) {
            throw new RuntimeException("Failed to load user save: " + response.message);
        }
        return response.payload;
    }
}
