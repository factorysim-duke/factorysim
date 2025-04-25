package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.jupiter.api.Test;

public class WorldBuilderConnectionTest {
  
  /** 
   * A simple ConnectionDTO constructor that uses reflection to set private fields
   */
  private ConnectionDTO mkDto(String src, String dst) {
    ConnectionDTO dto = new ConnectionDTO();
    try {
      Field f1 = ConnectionDTO.class.getDeclaredField("source");
      f1.setAccessible(true);
      f1.set(dto, src);
      Field f2 = ConnectionDTO.class.getDeclaredField("destination");
      f2.setAccessible(true);
      f2.set(dto, dst);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return dto;
  }

  /**
   * A StubSimulation that captures logs and connectBuildings calls
   */
  static class StubSimulation extends TestUtils.MockSimulation {
    World world; 
    int verbosity;
    List<String> logs = new ArrayList<>();
    boolean connected = false;
    String srcConnected, dstConnected;

    StubSimulation(World world, int verbosity) {
      super();
      this.world = world;
      this.verbosity = verbosity;
    }

    @Override public World getWorld() { return world; }
    @Override public int getVerbosity() { return verbosity; }
    @Override public Logger getLogger() {
      return msg -> logs.add(msg);
    }
    @Override public boolean connectBuildings(String src, String dst) {
      connected = true;
      srcConnected = src;
      dstConnected = dst;
      return true;
    }
  }

  /**
   * Calls the private buildConnections method using reflection
   */
  private void callBuildConnections(List<ConnectionDTO> dtos, StubSimulation sim) throws Exception {
    Method m = WorldBuilder.class
      .getDeclaredMethod("buildConnections", List.class, Simulation.class);
    m.setAccessible(true);
    m.invoke(null, dtos, sim);
  }

  @Test
  public void worldNotReady_logsWarning() throws Exception {
    StubSimulation sim = new StubSimulation(null, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Warning: World not set"));
    assertFalse(sim.connected);
  }

  @Test
  public void missingSource_logsAndSkip() throws Exception {
    // world exists but buildings list is empty
    World w = WorldBuilder.buildEmptyWorld();
    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("X","Y")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Source building 'X' does not exist"));
    assertFalse(sim.connected);
  }

  @Test
  public void missingDestination_logsAndSkip() throws Exception {
    // world has only "A" building
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));

    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","Z")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Destination building 'Z' does not exist"));
    assertFalse(sim.connected);
  }

  @Test
  public void connectThrows_logsError() throws Exception {
    // world has A and B buildings
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));
    w.getBuildings().add(new TestUtils.MockBuilding("B"));

    StubSimulation sim = new StubSimulation(w, 1) {
      @Override
      public boolean connectBuildings(String src, String dst) {
        throw new IllegalArgumentException("boom");
      }
    };
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Failed to create connection from A to B: boom"));
    assertFalse(sim.connected);
  }

  @Test
  public void successfulConnect_noLogs() throws Exception {
    // world has A and B buildings
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));
    w.getBuildings().add(new TestUtils.MockBuilding("B"));

    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertTrue(sim.connected);
    assertEquals("A", sim.srcConnected);
    assertEquals("B", sim.dstConnected);
    assertTrue(sim.logs.isEmpty());
  }
}
