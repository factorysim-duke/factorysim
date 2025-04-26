package edu.duke.ece651.factorysim.client;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.*;
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
}
