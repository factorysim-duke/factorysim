package edu.duke.ece651.factorysim;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {
  Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");
  ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
  Logger testLogger = new StreamLogger(new PrintStream(logOutput));

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
    sim.connectBuildings("W", "D");
    sim.connectBuildings("Hi", "D");
    sim.connectBuildings("Ha", "D");
    sim.connectBuildings("M", "Ha");
    sim.connectBuildings("M", "Hi");
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
  public void test_mine_verbosity_2() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 2, logger);

    Item wood = new Item("wood");
    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 1, 0), "W", sim);
    sim.onRecipeSelected(woodMine, new ReadyRequestPolicy(), Collections.emptyList(), null);
    sim.onSourceSelected(woodMine, new QLenSourcePolicy(), wood);
    sim.onIngredientSourceSelected(woodMine, wood, 0, wood, Collections.emptyList(), woodMine);

    assertEquals("", stream.toString()); // Nothing should be logged because building is not factory
  }

  @Test
  public void test_logging_verbosity_1() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 1, logger);
    sim.connectBuildings("W", "D");
    sim.connectBuildings("Hi", "D");
    sim.connectBuildings("Ha", "D");
    sim.connectBuildings("M", "Ha");
    sim.connectBuildings("M", "Hi");

    // 0> request 'door' from 'D'
    sim.makeUserRequest("door", "D");
    String expected = "[ingredient assignment]: wood assigned to W to deliver to D" + System.lineSeparator() +
        "[ingredient assignment]: handle assigned to Ha to deliver to D" + System.lineSeparator() +
        "[ingredient assignment]: metal assigned to M to deliver to Ha" + System.lineSeparator() +
        "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
        "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
        "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator();
    assertEquals(expected, stream.toString());
    stream.reset();

    // 0> step 50
    // Use `System.lineSeparator()` so tests can pass on Windows
    sim.step(50);
    expected =
        "[ingredient delivered]: wood to D from W on cycle 6" + System.lineSeparator() +
        "[ingredient delivered]: metal to Ha from M on cycle 6" + System.lineSeparator() +
        "    0: handle is ready" + System.lineSeparator() +
        "[ingredient delivered]: metal to Hi from M on cycle 6" + System.lineSeparator() +
        "    0: hinge is ready" + System.lineSeparator() +
        "[ingredient delivered]: metal to Hi from M on cycle 8" + System.lineSeparator() +
        "    0: hinge is ready" + System.lineSeparator() +
        "[ingredient delivered]: hinge to D from Hi on cycle 10" + System.lineSeparator() +
        "[ingredient delivered]: metal to Hi from M on cycle 10" + System.lineSeparator() +
        "    0: hinge is ready" + System.lineSeparator() +
        "[ingredient delivered]: hinge to D from Hi on cycle 12" + System.lineSeparator() +
        "[ingredient delivered]: handle to D from Ha on cycle 12" + System.lineSeparator() +
        "[ingredient delivered]: hinge to D from Hi on cycle 14" + System.lineSeparator() +
        "    0: door is ready" + System.lineSeparator() +
        "[order complete] Order 0 completed (door) at time 26" + System.lineSeparator();
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
    sim.connectBuildings("W", "D");
    sim.connectBuildings("Hi", "D");
    sim.connectBuildings("Ha", "D");
    sim.connectBuildings("M", "Ha");
    sim.connectBuildings("M", "Hi");
    // 0> request 'door' from 'D'
    sim.makeUserRequest("door", "D");
    String expected = "[source selection]: D (qlen) has request for door on 0" + System.lineSeparator() +
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
    assertThrows(IllegalArgumentException.class,
        () -> simulation.load("src/test/resources/inputs/test_load_producer_false"));
    assertThrows(IllegalArgumentException.class,
        () -> simulation.load("src/test/resources/inputs/test_load_recipe_false"));
    assertThrows(IllegalArgumentException.class,
        () -> simulation.load("src/test/resources/inputs/test_load_deliverTo_false"));
    assertThrows(IllegalArgumentException.class, () -> simulation.load("src/test/resources/inputs/invalid_file"));
    assertThrows(IllegalArgumentException.class, () -> simulation.save("invalid/0001/test_load_producer_false"));
  }

  @Test
  public void test_processRequest_policyReturnsNull() {
    // Initialize a mock building and policy
    Building building = new TestUtils.MockBuilding("TestBuilding");
    RequestPolicy mockPolicy = new RequestPolicy() {
      @Override
      public Request selectRequest(Building producer, List<Request> requests) {
        return null;
      }
      
      @Override
      public String getName() {
        return "mock";
      }
    };
    building.setRequestPolicy(mockPolicy);
    
    // Add a request to the building
    Item testItem = new Item("test");
    Recipe testRecipe = new Recipe(testItem, new HashMap<>(), 1);
    Request testRequest = new Request(1, testItem, testRecipe, building, null);
    building.prependPendingRequest(testRequest);
    assertEquals(mockPolicy.getName(), "mock");
    assertEquals(1, building.getNumOfPendingRequests());
    assertNull(building.getCurrentRequest());
    
    // Skip processing since policy returns null
    building.step();
    
    // Verify state remains unchanged
    assertEquals(1, building.getNumOfPendingRequests());
    assertNull(building.getCurrentRequest());
    assertTrue(building.getPendingRequests().contains(testRequest));
  }

  @Test
  public void test_load_edgeCases() {
    Simulation sim = new TestUtils.MockSimulation();

    // Missing `currentTime`
    sim.step(10);
    assertEquals(10, sim.getCurrentTime());
    sim.loadFromReader(new StringReader("""
    {
      "finished": false,
      "nextOrderNum": 0,
      "verbosity": 0,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """));
    assertEquals(0, sim.getCurrentTime());

    // `currentTime` is null
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": null,
      "finished": false,
      "nextOrderNum": 0,
      "verbosity": 0,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // `currentTime` is null
    sim.step(10);
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": null,
      "finished": false,
      "nextOrderNum": 0,
      "verbosity": 0,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // Missing `finished`
    sim.finish();
    assertTrue(sim.isFinished());
    sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "nextOrderNum": 1,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """));
    assertFalse(sim.isFinished());

    // `finished` is null
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": null,
      "nextOrderNum": 1,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // `finished` is not boolean
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": "abc",
      "nextOrderNum": 1,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": [],
      "nextOrderNum": 1,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // Missing `nextOrderNum`
    sim.getOrderNum(); // `nextOrderNum` = 1 now
    sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """));
    assertEquals(0, sim.getOrderNum()); // ⚠: `getOrderNum` increments `nextOrderNum` field

    // `nextOrderNum` is null
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "nextOrderNum": null,
      "verbosity": 1,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // Missing `verbosity`
    sim.setVerbosity(2);
    sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "nextOrderNum": 2,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """));
    assertEquals(0, sim.getVerbosity());

    // `verbosity` is null
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "nextOrderNum": 2,
      "verbosity": null,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // `verbosity` is not integer
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "nextOrderNum": 2,
      "verbosity": false,
      "types": [],
      "buildings": [],
      "recipes": []
    }
    """)));

    // `requests` is null
    assertThrows(IllegalArgumentException.class, () -> sim.loadFromReader(new StringReader("""
    {
      "currentTime": 5,
      "finished": true,
      "nextOrderNum": 2,
      "verbosity": 0,
      "types": [],
      "buildings": [],
      "recipes": [],
      "requests": null
    }
    """)));
  }
  @Test
  public void test_tileMap() {
    assertEquals(TileType.ROAD, sim.checkTile(new Coordinate(6,6)));
    assertEquals(TileType.BUILDING, sim.checkTile(new Coordinate(0,0)));
    assertEquals(new Coordinate(0,0),sim.getBuildingLocation("D"));
    sim.updateTileMap(new Coordinate(6,6),TileType.PATH);
    assertEquals(TileType.PATH, sim.checkTile(new Coordinate(6,6)));
  }

  @Test
  public void test_connectBuildings_validAndCache() {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");

    simulation.setLogger(testLogger);

    boolean firstConnection = simulation.connectBuildings("D", "W");
    assertTrue(firstConnection);
    firstConnection = simulation.connectBuildings("W", "D");
    assertTrue(firstConnection);

    String logs = logOutput.toString();
    assertFalse(logs.contains("Path already exists in cache."));

    logOutput.reset();
    boolean secondConnection = simulation.connectBuildings("W", "D");
    assertTrue(secondConnection);
    simulation.getPathList().getFirst().dump();
    simulation.getPathList().getLast().dump();

    assertTrue(simulation.checkUsage(new Coordinate(0,0)));
    assertTrue(simulation.checkUsage(new Coordinate(0,1)));
    boolean removeConnection = simulation.disconnectBuildings("W", "D");
    assertFalse(simulation.checkUsage(new Coordinate(1,0)));
    assertTrue(removeConnection);

    boolean secondRemove = simulation.disconnectBuildings("W", "D");
    assertFalse(secondRemove);
  }

  @Test
  public void test_connectBuildings_noValidPath() {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");

    simulation.setLogger(testLogger);

    Coordinate src = simulation.getBuildingLocation("W");
    Coordinate dst = simulation.getBuildingLocation("D");

    // set all tiles to building except src and dst
    for (int x = 0; x < simulation.getWorld().getTileMap().getWidth(); x++) {
      for (int y = 0; y < simulation.getWorld().getTileMap().getHeight(); y++) {
        Coordinate coord = new Coordinate(x, y);
        if (!coord.equals(src) && !coord.equals(dst)) {
          simulation.getWorld().getTileMap().setTileType(coord, TileType.BUILDING);
        }
      }
    }


    assertThrows(IllegalArgumentException.class, () -> simulation.connectBuildings("W", "D"));

    Building W = simulation.getWorld().getBuildingFromName("W");
    Building D = simulation.getWorld().getBuildingFromName("D");
    Item wood = new Item("wood");
    Delivery d=new Delivery(W,D,wood,1,5);
    assertThrows(IllegalArgumentException.class, () -> sim.addDelivery(W,D,wood,1));
  }

  @Test
  public void test_connectBuildings_cacheMissingEntry() throws Exception {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");

    ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
    Logger testLogger = new StreamLogger(new PrintStream(logOutput));
    simulation.setLogger(testLogger);

    boolean firstConnection = simulation.connectBuildings("W", "D");
    assertTrue(firstConnection);

    java.lang.reflect.Field field = Simulation.class.getDeclaredField("pathList");
    field.setAccessible(true);
    List<Path> pathList = (List<Path>) field.get(simulation);
    Coordinate src = simulation.getBuildingLocation("W");
    Coordinate dst = simulation.getBuildingLocation("D");
    assertFalse(pathList.isEmpty());
    JsonArray jsonArray = simulation.pathListToJson();
    assertEquals(pathList.size(), jsonArray.size());
    JsonObject pathJson = jsonArray.get(0).getAsJsonObject();

//
//    logOutput.reset();
//    boolean secondConnection = simulation.connectBuildings("W", "D");
//    assertTrue(secondConnection);
//
//    String logs = logOutput.toString();
//    assertFalse(logs.contains("Path already exists in cache."));
  }

    @Test
    public void test_getBuildingNameByCoordinate() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        String b=sim.getBuildingNameByCoordinate(new Coordinate(9,9));
        assertNull(b);
    }

    @Test
    public void test_getDeliveryCoordinates() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        assertEquals(0,sim.getDeliveryCoordinates().size());
        Building W = simulation.getWorld().getBuildingFromName("W");
        Building D = simulation.getWorld().getBuildingFromName("D");
        Item wood = new Item("wood");
        Delivery d=new Delivery(W,D,wood,1,5);
        simulation.connectBuildings(W,D);
        simulation.addDelivery(W,D,wood,1);
        List<Coordinate> coordinates = simulation.getDeliveryCoordinates();
        assertEquals(1, coordinates.size());
        assertEquals(W.getLocation(), coordinates.get(0));

        boolean removeConnection = simulation.disconnectBuildings("W", "D");
        assertFalse(removeConnection);
    }

  @Test
  public void testBuildDeliveries() throws Exception {
    JsonArray deliveries = new JsonArray();

    JsonObject d1 = new JsonObject();
    d1.addProperty("source", "W");
    d1.addProperty("destination", "D");
    d1.addProperty("item", "metal");
    d1.addProperty("quantity", 1);
    d1.addProperty("deliveryTime", 5);
    d1.addProperty("pathIndex", 0);
    d1.addProperty("stepIndex", 0);
    d1.addProperty("x", 1);
    d1.addProperty("y", 2);

    deliveries.add(d1);

    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");

    Field f = Simulation.class.getDeclaredField("deliverySchedule");
    f.setAccessible(true);
    DeliverySchedule schedule = (DeliverySchedule) f.get(simulation);

    assertEquals(0, schedule.toJson().size());

    Method method = Simulation.class.getDeclaredMethod("buildDeliveries", JsonArray.class);
    method.setAccessible(true);
    method.invoke(simulation, deliveries);

    JsonArray jsonAfter = schedule.toJson();
    assertEquals(1, jsonAfter.size());

    JsonObject deliveryJson = jsonAfter.get(0).getAsJsonObject();
    assertEquals("W", deliveryJson.get("source").getAsString());
    assertEquals("D", deliveryJson.get("destination").getAsString());
    assertEquals("metal", deliveryJson.get("item").getAsString());
    assertEquals(1, deliveryJson.get("quantity").getAsInt());
    assertEquals(5, deliveryJson.get("deliveryTime").getAsInt());
    assertEquals(0, deliveryJson.get("pathIndex").getAsInt());
    assertEquals(0, deliveryJson.get("stepIndex").getAsInt());
    assertEquals(1, deliveryJson.get("x").getAsInt());
    assertEquals(2, deliveryJson.get("y").getAsInt());
  }

  @Test
  public void test_constructor_World_int_Logger() {
    World world = WorldBuilder.buildEmptyWorld(10, 10);
    Logger logger = new StreamLogger(System.out);
    Simulation sim = new Simulation(world, 3, logger);
    assertSame(world, sim.getWorld());
    assertSame(logger, sim.getLogger());
    assertEquals(3, sim.getVerbosity());
  }
}
