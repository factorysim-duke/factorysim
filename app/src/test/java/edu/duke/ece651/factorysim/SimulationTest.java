package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

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

  @Test
  public void test_logger_getter_setter() {
    Simulation sim = new TestUtils.MockSimulation();
    Logger logger = new StreamLogger(System.out);
    sim.setLogger(logger); // This is the default logger, but just to be safe in this test
    assertSame(logger, sim.getLogger());
  }

  @Test
  public void test_onRequestCompleted() {
    Simulation sim = new TestUtils.MockSimulation();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    sim.setLogger(logger);

    sim.onRequestCompleted(new Request(0, new Item("wood"),
            TestUtils.makeTestRecipe("wood", 1, 2),
            null, null));
    assertEquals("[order complete] Order 0 completed (wood) at time 0" + System.lineSeparator(),
            stream.toString());
  }
}
