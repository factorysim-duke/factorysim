package edu.duke.ece651.factorysim.client;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public class ServerConnection implements Closeable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final Gson gson;

    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.gson = new Gson();
    }

    protected ServerConnection(Socket socket, BufferedReader in, BufferedWriter out, Gson gson) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.gson = gson;
    }

    private ServerMessage sendMessage(ClientMessage message) throws IOException {
        String json = gson.toJson(message);
        out.write(json);
        out.newLine();
        out.flush();

        String responseLine = in.readLine();
        if (responseLine == null) {
            throw new IOException("Server closed connection unexpectedly");
        }
        return gson.fromJson(responseLine, ServerMessage.class);
    }

    public ServerMessage signup(String user, String password) throws IOException {
        ClientMessage msg = new ClientMessage("signup", user, password);
        return sendMessage(msg);
    }

    public ServerMessage login(String user, String password) throws IOException {
        ClientMessage msg = new ClientMessage("login", user, password);
        return sendMessage(msg);
    }

    public ServerMessage save(String user, String save) throws IOException {
        ClientMessage msg = new ClientMessage("save", user, save);
        return sendMessage(msg);
    }

    public ServerMessage loadPreset(String preset) throws IOException {
        ClientMessage msg = new ClientMessage("load", null, preset);
        return sendMessage(msg);
    }

    public ServerMessage loadUserSave() throws IOException {
        ClientMessage msg = new ClientMessage("load", null, null);
        return sendMessage(msg);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        try {
            in.close();
        } catch (IOException ignored) {}
        try {
            out.close();
        } catch (IOException ignored) {}
    }
}
