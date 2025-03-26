package edu.duke.ece651.factorysim;

import java.io.*;
import java.util.*;

import com.google.gson.JsonSyntaxException;
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

    assertThrows(IllegalArgumentException.class, () -> sim.step(0));
    assertThrows(IllegalArgumentException.class, () -> sim.step(Integer.MAX_VALUE));
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
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 0, logger);

    sim.onRequestCompleted(new Request(0, new Item("wood"),
            TestUtils.makeTestRecipe("wood", 1, 2),
            null, null));
    assertEquals("[order complete] Order 0 completed (wood) at time 0" + System.lineSeparator(),
            stream.toString());
    stream.reset();

    sim.setVerbosity(-1);
    sim.onRequestCompleted(new Request(0, new Item("wood"),
            TestUtils.makeTestRecipe("wood", 1, 2),
            null, null));
    assertEquals("", stream.toString());
  }

  @Test
  public void test_onIngredientDelivered() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 1, logger);

    Item wood = new Item("wood");
    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 1, 0), "W", sim);
    sim.onIngredientDelivered(wood, woodMine, woodMine);

    assertEquals("[ingredient delivered]: wood to W from W on cycle 0" + System.lineSeparator(), stream.toString());
  }

  @Test
  public void test_onIngredientSourceSelected() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 2, logger);

    Item wood = new Item("wood");
    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 1, 0), "W", sim);
    sim.onIngredientSourceSelected(woodMine, wood, 0, wood, Collections.emptyList(), woodMine);

    assertEquals("", stream.toString()); // Nothing should be logged because building is not factory
  }

  @Test
  public void test_logging_verbosity_1() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 1, logger);

    // 0> request 'door' from 'D'
    sim.makeUserRequest("door", "D");
    String expected = "[ingredient assignment]: wood assigned to W to deliver to D"    + System.lineSeparator() +
                      "[ingredient assignment]: handle assigned to Ha to deliver to D" + System.lineSeparator() +
                      "[ingredient assignment]: metal assigned to M to deliver to Ha"  + System.lineSeparator() +
                      "[ingredient assignment]: hinge assigned to Hi to deliver to D"  + System.lineSeparator() +
                      "[ingredient assignment]: metal assigned to M to deliver to Hi"  + System.lineSeparator() +
                      "[ingredient assignment]: hinge assigned to Hi to deliver to D"  + System.lineSeparator() +
                      "[ingredient assignment]: metal assigned to M to deliver to Hi"  + System.lineSeparator() +
                      "[ingredient assignment]: hinge assigned to Hi to deliver to D"  + System.lineSeparator() +
                      "[ingredient assignment]: metal assigned to M to deliver to Hi"  + System.lineSeparator();
    assertEquals(expected, stream.toString());
    stream.reset();

    // 0> step 50
    sim.step(50);
    expected = "[ingredient delivered]: wood to D from W on cycle 1"    + System.lineSeparator() +
               "[ingredient delivered]: metal to Hi from M on cycle 1"  + System.lineSeparator() +
               "    0: hinge is ready"                                  + System.lineSeparator() +
               "[ingredient delivered]: hinge to D from Hi on cycle 3"  + System.lineSeparator() +
               "[ingredient delivered]: metal to Hi from M on cycle 3"  + System.lineSeparator() +
               "    0: hinge is ready"                                  + System.lineSeparator() +
               "[ingredient delivered]: hinge to D from Hi on cycle 5"  + System.lineSeparator() +
               "[ingredient delivered]: metal to Hi from M on cycle 5"  + System.lineSeparator() +
               "    0: hinge is ready"                                  + System.lineSeparator() +
               "[ingredient delivered]: hinge to D from Hi on cycle 7"  + System.lineSeparator() +
               "[ingredient delivered]: metal to Ha from M on cycle 7"  + System.lineSeparator() +
               "    0: handle is ready"                                 + System.lineSeparator() +
               "[ingredient delivered]: handle to D from Ha on cycle 13"+ System.lineSeparator() +
               "    0: door is ready"                                   + System.lineSeparator() +
               "[order complete] Order 0 completed (door) at time 26"   + System.lineSeparator();
    assertEquals(expected, stream.toString());
    stream.reset();

    // 50> finish
    sim.finish();
    expected = "Simulation completed at time-step 50" + System.lineSeparator();
    assertEquals(expected, stream.toString());
  }

  @Test
  public void test_logging_verbosity_2() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 2, logger);

    // 0> request 'door' from 'D'
    sim.makeUserRequest("door", "D");
    String expected =
            "[source selection]: D (qlen) has request for door on 0" + System.lineSeparator() +
            "[D:door:0] For ingredient wood" + System.lineSeparator() +
            "    W: 0" + System.lineSeparator() +
            "    Selecting W" + System.lineSeparator() +
            "[ingredient assignment]: wood assigned to W to deliver to D" + System.lineSeparator() +
            "[D:door:1] For ingredient handle" + System.lineSeparator() +
            "    Ha: 0" + System.lineSeparator() +
            "    Selecting Ha" + System.lineSeparator() +
            "[ingredient assignment]: handle assigned to Ha to deliver to D" + System.lineSeparator() +
            "[source selection]: Ha (qlen) has request for handle on 0" + System.lineSeparator() +
            "[Ha:handle:0] For ingredient metal" + System.lineSeparator() +
            "    M: 0" + System.lineSeparator() +
            "    Selecting M" + System.lineSeparator() +
            "[ingredient assignment]: metal assigned to M to deliver to Ha" + System.lineSeparator() +
            "[D:door:2] For ingredient hinge" + System.lineSeparator() +
            "    Hi: 0" + System.lineSeparator() +
            "    Selecting Hi" + System.lineSeparator() +
            "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
            "[source selection]: Hi (qlen) has request for hinge on 0" + System.lineSeparator() +
            "[Hi:hinge:0] For ingredient metal" + System.lineSeparator() +
            "    M: 1" + System.lineSeparator() +
            "    Selecting M" + System.lineSeparator() +
            "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
            "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
            "[source selection]: Hi (qlen) has request for hinge on 0" + System.lineSeparator() +
            "[Hi:hinge:0] For ingredient metal" + System.lineSeparator() +
            "    M: 2" + System.lineSeparator() +
            "    Selecting M" + System.lineSeparator() +
            "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
            "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
            "[source selection]: Hi (qlen) has request for hinge on 0" + System.lineSeparator() +
            "[Hi:hinge:0] For ingredient metal" + System.lineSeparator() +
            "    M: 3" + System.lineSeparator() +
            "    Selecting M" + System.lineSeparator() +
            "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator();
    assertEquals(expected, stream.toString());
    stream.reset();

    // 0> step 50
    sim.step(50);
    stream.reset(); // Logs nothing about verbosity 2, so just ignores for now

    // 50> finish
    sim.finish();
    expected = "Simulation completed at time-step 50" + System.lineSeparator();
    assertEquals(expected, stream.toString());
  }

  @Test
  public void test_SaveAndLoad_Simulation() throws Exception {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
    simulation.setVerbosity(2);



    simulation.makeUserRequest("hinge", "Hi");

    simulation.step(1);
    simulation.makeUserRequest("metal", "M");
    assertEquals(1, simulation.getCurrentTime());

    simulation.save("test_save");
    assertTrue(new File("test_save").exists());

    Simulation loadedSimulation = new Simulation("src/test/resources/inputs/doors1.json");
    loadedSimulation.load("test_save");

    assertEquals(1, loadedSimulation.getCurrentTime());
    assertEquals(2, loadedSimulation.getVerbosity());

    assertFalse(loadedSimulation.allRequestsFinished());
  }

  @Test
  public void test_load_save_false() throws Exception {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
    assertThrows(IllegalArgumentException.class,() -> simulation.load("src/test/resources/inputs/test_load_producer_false"));
    assertThrows(IllegalArgumentException.class,() -> simulation.load("src/test/resources/inputs/test_load_recipe_false"));
    assertThrows(IllegalArgumentException.class,() -> simulation.load("src/test/resources/inputs/test_load_deliverTo_false"));
    assertThrows(IllegalArgumentException.class,() -> simulation.load("src/test/resources/inputs/invalid_file"));
    assertThrows(IllegalArgumentException.class,() -> simulation.save("invalid/0001/test_load_producer_false"));

  }
}
