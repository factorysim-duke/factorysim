package edu.duke.ece651.factorysim.client;

public class ClientMessage {
    public String type;
    public String user;
    public String payload;

    public ClientMessage(String type, String user, String payload) {
        this.type = type;
        this.user = user;
        this.payload = payload;
    }
}
