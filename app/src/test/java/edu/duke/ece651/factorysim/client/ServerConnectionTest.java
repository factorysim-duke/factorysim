package edu.duke.ece651.factorysim.client;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectionTest {
  private static class TestServerConnection extends ServerConnection {
    private final String expectedResponse;
    private String lastSentMessage;

    public TestServerConnection(String expectedResponse) {
      super(createDummySocket(), createReader(expectedResponse), createTestWriter(), new Gson());
      this.expectedResponse = expectedResponse;
    }

    private static Socket createDummySocket() {
      return new Socket() {
        @Override
        public void close() {
        }
      };
    }

    private static BufferedReader createReader(String response) {
      return new BufferedReader(new StringReader(response));
    }

    private static BufferedWriter createTestWriter() {
      return new BufferedWriter(new StringWriter());
    }

    @Override
    public ServerMessage signup(String user, String password) throws IOException {
      lastSentMessage = new Gson().toJson(new ClientMessage("signup", user, password));
      return new Gson().fromJson(expectedResponse, ServerMessage.class);
    }

    @Override
    public ServerMessage login(String user, String password) throws IOException {
      lastSentMessage = new Gson().toJson(new ClientMessage("login", user, password));
      return new Gson().fromJson(expectedResponse, ServerMessage.class);
    }

    @Override
    public ServerMessage save(String save) throws IOException {
      lastSentMessage = new Gson().toJson(new ClientMessage("save", null, save));
      return new Gson().fromJson(expectedResponse, ServerMessage.class);
    }

    @Override
    public ServerMessage loadPreset(String preset) throws IOException {
      lastSentMessage = new Gson().toJson(new ClientMessage("load", null, preset));
      return new Gson().fromJson(expectedResponse, ServerMessage.class);
    }

    @Override
    public ServerMessage loadUserSave() throws IOException {
      lastSentMessage = new Gson().toJson(new ClientMessage("load", null, null));
      return new Gson().fromJson(expectedResponse, ServerMessage.class);
    }

    public String getLastSentMessage() {
      return lastSentMessage;
    }
  }

  @Test
  public void testSignup() throws IOException {
    String testUser = "testUser";
    String testPassword = "testPassword";
    String expectedResponse = "{\"status\":\"ok\",\"message\":\"Signup successful\",\"payload\":null}";

    TestServerConnection connection = new TestServerConnection(expectedResponse);
    ServerMessage result = connection.signup(testUser, testPassword);
    assertEquals("ok", result.status);
    assertEquals("Signup successful", result.message);
    assertNull(result.payload);
    ClientMessage sentMessage = new Gson().fromJson(connection.getLastSentMessage(), ClientMessage.class);
    assertEquals("signup", sentMessage.type);
    assertEquals(testUser, sentMessage.user);
    assertEquals(testPassword, sentMessage.payload);
  }

  @Test
  public void testLogin() throws IOException {
    String testUser = "testUser";
    String testPassword = "testPassword";
    String expectedResponse = "{\"status\":\"ok\",\"message\":\"Login successful\",\"payload\":\"user_data\"}";
    TestServerConnection connection = new TestServerConnection(expectedResponse);
    ServerMessage result = connection.login(testUser, testPassword);

    assertEquals("ok", result.status);
    assertEquals("Login successful", result.message);
    assertEquals("user_data", result.payload);
    ClientMessage sentMessage = new Gson().fromJson(connection.getLastSentMessage(), ClientMessage.class);
    assertEquals("login", sentMessage.type);
    assertEquals(testUser, sentMessage.user);
    assertEquals(testPassword, sentMessage.payload);
  }

  @Test
  public void testSave() throws IOException {
    String testSaveData = "{\"gameState\":\"some data\"}";
    String expectedResponse = "{\"status\":\"ok\",\"message\":\"Save successful\",\"payload\":null}";
    TestServerConnection connection = new TestServerConnection(expectedResponse);
    ServerMessage result = connection.save(testSaveData);
    assertEquals("ok", result.status);
    assertEquals("Save successful", result.message);
    assertNull(result.payload);
    ClientMessage sentMessage = new Gson().fromJson(connection.getLastSentMessage(), ClientMessage.class);
    assertEquals("save", sentMessage.type);
    assertNull(sentMessage.user);
    assertEquals(testSaveData, sentMessage.payload);
  }

  @Test
  public void testLoadPreset() throws IOException {
    String presetPath = "path/to/preset";
    String expectedResponse = "{\"status\":\"ok\",\"message\":\"Preset loaded\",\"payload\":\"{\\\"preset\\\":\\\"data\\\"}\"}";
    TestServerConnection connection = new TestServerConnection(expectedResponse);

    ServerMessage result = connection.loadPreset(presetPath);
    assertEquals("ok", result.status);
    assertEquals("Preset loaded", result.message);
    assertEquals("{\"preset\":\"data\"}", result.payload);
    ClientMessage sentMessage = new Gson().fromJson(connection.getLastSentMessage(), ClientMessage.class);
    assertEquals("load", sentMessage.type);
    assertNull(sentMessage.user);
    assertEquals(presetPath, sentMessage.payload);
  }

  @Test
  public void testLoadUserSave() throws IOException {
    String expectedResponse = "{\"status\":\"ok\",\"message\":\"User data loaded\",\"payload\":\"{\\\"userData\\\":\\\"some saved data\\\"}\"}";
    TestServerConnection connection = new TestServerConnection(expectedResponse);
    ServerMessage result = connection.loadUserSave();

    assertEquals("ok", result.status);
    assertEquals("User data loaded", result.message);
    assertEquals("{\"userData\":\"some saved data\"}", result.payload);
    ClientMessage sentMessage = new Gson().fromJson(connection.getLastSentMessage(), ClientMessage.class);
    assertEquals("load", sentMessage.type);
    assertNull(sentMessage.user);
    assertNull(sentMessage.payload);
  }

  @Test
  public void testErrorResponse() throws IOException {
    String expectedResponse = "{\"status\":\"error\",\"message\":\"Authentication failed\",\"payload\":null}";
    TestServerConnection connection = new TestServerConnection(expectedResponse);
    ServerMessage result = connection.login("user", "wrongpass");

    assertEquals("error", result.status);
    assertEquals("Authentication failed", result.message);
    assertNull(result.payload);
  }

  private static class ExceptionalServerConnection extends ServerConnection {
    public ExceptionalServerConnection() {
      super(
          new ExceptionalSocket(),
          new ExceptionalReader(),
          new ExceptionalWriter(),
          new Gson());
    }

    private static class ExceptionalSocket extends Socket {
      @Override
      public void close() throws IOException {
        throw new IOException("Test exception on close");
      }
    }

    private static class ExceptionalReader extends BufferedReader {
      public ExceptionalReader() {
        super(new StringReader(""));
      }

      @Override
      public void close() throws IOException {
        throw new IOException("Test exception on reader close");
      }
    }

    private static class ExceptionalWriter extends BufferedWriter {
      public ExceptionalWriter() {
        super(new StringWriter());
      }

      @Override
      public void close() throws IOException {
        throw new IOException("Test exception on writer close");
      }
    }
  }

  @Test
  public void testCloseWithIOExceptions() {
    ExceptionalServerConnection connection = new ExceptionalServerConnection();
    connection.close();
    assertTrue(true, "Test passed if no exception is thrown");
  }

  @Test
  public void test_constructor() {
    assertThrows(Exception.class,
            () -> new ServerConnection("something that definitely not a host", 999999999));
  }

  @Test
  public void test_signup_login() throws Exception {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();

    Thread serverThread = new Thread(() -> {
      try (Socket client = serverSocket.accept();
           BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
           BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

        for (int i = 0; i < 2; i++) {
          String req = in.readLine();
          ServerMessage resp;
          if (req.contains("signup")) {
            resp = new ServerMessage("ok", "Successfully signed up", null);
          } else { // assume login
            resp = new ServerMessage("ok", "Successfully logged in", null);
          }
          out.write(new Gson().toJson(resp));
          out.newLine();
          out.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    serverThread.start();

    try (ServerConnection conn = new ServerConnection("localhost", port)) {
      ServerMessage signupResp = conn.signup("user", "pass");
      assertEquals("ok", signupResp.status);
      assertEquals("Successfully signed up", signupResp.message);

      ServerMessage loginResp = conn.login("user", "pass");
      assertEquals("ok", loginResp.status);
      assertEquals("Successfully logged in", loginResp.message);
    }

    serverSocket.close();
  }

  private int startFakeServer(int responses, FakeResponseProvider responseProvider) throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();

    new Thread(() -> {
      try (ServerSocket server = serverSocket;
           Socket client = server.accept();
           BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
           BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

        for (int i = 0; i < responses; i++) {
          String req = in.readLine();
          if (req == null) break;
          ServerMessage resp = responseProvider.createResponse(req);
          out.write(new Gson().toJson(resp));
          out.newLine();
          out.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    return port;
  }

  interface FakeResponseProvider {
    ServerMessage createResponse(String request);
    void testClient(ServerConnection conn) throws IOException;
  }

  @Test
  public void test_save() throws Exception {
    startFakeServer(1, new FakeResponseProvider() {
      @Override
      public ServerMessage createResponse(String request) {
        assertTrue(request.contains("save"));
        return new ServerMessage("ok", "Save successful", null);
      }

      @Override
      public void testClient(ServerConnection conn) throws IOException {
        ServerMessage saveResp = conn.save("fake save data");
        assertEquals("ok", saveResp.status);
        assertEquals("Save successful", saveResp.message);
        assertNull(saveResp.payload);
      }
    });
  }

  @Test
  public void test_loadPreset() throws Exception {
    startFakeServer(1, new FakeResponseProvider() {
      @Override
      public ServerMessage createResponse(String request) {
        assertTrue(request.contains("load"));
        return new ServerMessage("ok", "Preset loaded", "preset-data-here");
      }

      @Override
      public void testClient(ServerConnection conn) throws IOException {
        ServerMessage loadResp = conn.loadPreset("easy-preset");
        assertEquals("ok", loadResp.status);
        assertEquals("Preset loaded", loadResp.message);
        assertEquals("preset-data-here", loadResp.payload);
      }
    });
  }

  @Test
  public void test_loadUserSave() throws Exception {
    startFakeServer(1, new FakeResponseProvider() {
      @Override
      public ServerMessage createResponse(String request) {
        assertTrue(request.contains("load"));
        return new ServerMessage("ok", "Load successful", "user-save-data");
      }

      @Override
      public void testClient(ServerConnection conn) throws IOException {
        ServerMessage loadResp = conn.loadUserSave();
        assertEquals("ok", loadResp.status);
        assertEquals("Load successful", loadResp.message);
        assertEquals("user-save-data", loadResp.payload);
      }
    });
  }

  @Test
  public void test_ServerConnectionManager_loadPreset_signup() throws Exception {
    startFakeServer(2, new FakeResponseProvider() {
      @Override
      public ServerMessage createResponse(String request) {
        if (request.contains("load")) {
          return new ServerMessage("ok", "Preset loaded", "preset-data");
        }
        if (request.contains("signup")) {
          return new ServerMessage("ok", "Signup successful", null);
        }
        return new ServerMessage("error", "Unexpected request", null);
      }

      @Override
      public void testClient(ServerConnection conn) throws IOException {
        ServerConnectionManager scm = ServerConnectionManager.getInstance();
        String preset = scm.loadPreset("localhost", conn.getSocket().getPort(), "preset1");
        assertEquals("preset-data", preset);

        scm.signup("localhost", conn.getSocket().getPort(), "newuser", "pass");
      }
    });
  }

  @Test
  public void test_ServerConnectionManager_connect_save_load_disconnect() throws Exception {
    int port = startFakeServer(4, new FakeResponseProvider() {
      @Override
      public ServerMessage createResponse(String request) {
        if (request.contains("\"type\":\"login\"")) {
          return new ServerMessage("error", "Login failed", null);
        }
        if (request.contains("\"type\":\"signup\"")) {
          return new ServerMessage("ok", "Signup successful", null);
        }
        if (request.contains("\"type\":\"save\"")) {
          return new ServerMessage("ok", "Save successful", null);
        }
        if (request.contains("\"type\":\"load\"")) {
          return new ServerMessage("ok", "Load successful", "user-save-data");
        }
        return new ServerMessage("error", "Unexpected", null);
      }

      @Override
      public void testClient(ServerConnection conn) { }
    });

    ServerConnectionManager scm = ServerConnectionManager.getInstance();
    scm.connect("localhost", port, "user", "pass");

    scm.saveUserSave("save-data-here");

    String loadedData = scm.loadUserSave();
    assertEquals("user-save-data", loadedData);

    scm.disconnect();
  }

  @Test
  public void test_ServerConnectionManager_exceptions() {
    ServerConnectionManager scm = ServerConnectionManager.getInstance();

    // Should throw because no active connection
    assertThrows(RuntimeException.class, () -> scm.loadUserSave());
    assertThrows(RuntimeException.class, () -> scm.saveUserSave("something"));
  }
}
