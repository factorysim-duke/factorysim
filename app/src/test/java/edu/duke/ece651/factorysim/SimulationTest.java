package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {
  Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

  @Test
  void step() {
    sim.step(1);
    assertEquals(1, sim.getCurrentTime());
    sim.step(2);
    assertEquals(3, sim.getCurrentTime());
  }

  @Test
  void testValidRequest() {
    assertDoesNotThrow(() -> sim.makeUserRequest("door", "D"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("door", "Z"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("invalidItem", "D"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("hinge", "D"));
  }

  @Test
  void finish() {
    sim.finish();
    assertTrue(sim.isFinished());
  }

  @Test
  void testAllRequestsFinished() {
    sim.makeUserRequest("door", "D");
    assertFalse(sim.allRequestsFinished());
    sim.finish();
    assertTrue(sim.allRequestsFinished());
  }

  @Test
  void isFinished() {
    assertFalse(sim.isFinished());
  }

  @Test
  void getCurrentTime() {
    assertEquals(0, sim.getCurrentTime());
  }
}
