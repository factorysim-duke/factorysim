package edu.duke.ece651.factorysim.client;

public class NullServerConnection extends ServerConnection {
  public NullServerConnection() {
    super(null, null, null, null);
  }

  @Override
  public ServerMessage signup(String user, String password) {
    return new ServerMessage("ok", "", null);
  }

  @Override
  public ServerMessage login(String user, String password) {
    return new ServerMessage("ok", "", null);
  }

  @Override
  public ServerMessage save(String user, String save) {
    return new ServerMessage("ok", "", null);
  }

  @Override
  public ServerMessage loadPreset(String preset) {
    return new ServerMessage("ok", "", null);
  }

  @Override
  public ServerMessage loadUserSave(String user) {
    return new ServerMessage("ok", "", null);
  }

  @Override
  public void close() { }
}
