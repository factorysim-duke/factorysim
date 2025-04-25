package edu.duke.ece651.factorysim.server;

public class ServerMessage {
    public String status;
    public String message;
    public String payload;

    public ServerMessage(String status, String message, String payload) {
        this.status = status;
        this.message = message;
        this.payload = payload;
    }
}
