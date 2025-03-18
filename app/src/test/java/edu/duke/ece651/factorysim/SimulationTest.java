package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {
  Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

  @Test
  public void test_step() {
    sim.step(1);
    assertEquals(1, sim.getCurrentTime());
    sim.step(2);
    assertEquals(3, sim.getCurrentTime());
  }

  @Test
  public void test_valid_request() {
    assertDoesNotThrow(() -> sim.makeUserRequest("door", "D"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("door", "Z"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("invalidItem", "D"));
    assertThrows(IllegalArgumentException.class, () -> sim.makeUserRequest("hinge", "D"));
  }

  @Test
  public void test_finish() {
    sim.finish();
    assertTrue(sim.isFinished());
  }

  @Test
  public void test_all_requests_finished() {
    sim.makeUserRequest("door", "D");
    assertFalse(sim.allRequestsFinished());
    sim.finish();
    assertTrue(sim.allRequestsFinished());
  }

  @Test
  public void test_is_finished() {
    assertFalse(sim.isFinished());
  }

  @Test
  public void test_get_current_time() {
    assertEquals(0, sim.getCurrentTime());
  }

  @Test
  public void test_set_invalid_policy() {
    assertThrows(IllegalArgumentException.class, () -> sim.setPolicy("invalidPolicyType", "fifo", "*"));
  }
}
