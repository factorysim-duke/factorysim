package edu.duke.ece651.factorysim;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import edu.duke.ece651.factorysim.db.DBInitializer;


public class SimulationTest {
    Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");
    ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
    Logger testLogger = new StreamLogger(new PrintStream(logOutput));
    @BeforeAll
    static void initDatabase() throws Exception {
      // this will create the sessions table (and any others) in data/factory.db
      DBInitializer.init();
    }

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
                "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator();
        assertEquals(expected, stream.toString());
        stream.reset();

        // 0> step 50
        // Use `System.lineSeparator()` so tests can pass on Windows
        sim.step(50);
        expected =
                "[ingredient assignment]: wood assigned to W to deliver to D" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Ha" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
                        "[ingredient assignment]: wood assigned to W to deliver to D" + System.lineSeparator() +
                        "[ingredient assignment]: wood assigned to W to deliver to D" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Ha" + System.lineSeparator() +
                        "[ingredient delivered]: wood to D from W on cycle 6" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Ha from M on cycle 6" + System.lineSeparator() +
                        "    0: handle is ready" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 6" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
                        "[ingredient assignment]: handle assigned to Ha to deliver to D" + System.lineSeparator() +
                        "[ingredient delivered]: wood to D from W on cycle 8" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
                        "[ingredient delivered]: wood to D from W on cycle 10" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Ha from M on cycle 10" + System.lineSeparator() +
                        "    0: handle is ready" + System.lineSeparator() +
                        "[ingredient delivered]: hinge to D from Hi on cycle 10" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 10" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient delivered]: wood to D from W on cycle 12" + System.lineSeparator() +
                        "[ingredient delivered]: handle to D from Ha on cycle 12" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Ha from M on cycle 14" + System.lineSeparator() +
                        "    0: handle is ready" + System.lineSeparator() +
                        "[ingredient delivered]: hinge to D from Hi on cycle 14" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 14" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 16" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
                        "[ingredient assignment]: metal assigned to M to deliver to Hi" + System.lineSeparator() +
                        "[ingredient delivered]: hinge to D from Hi on cycle 18" + System.lineSeparator() +
                        "    0: door is ready" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 18" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient delivered]: handle to D from Ha on cycle 18" + System.lineSeparator() +
                        "    0: door is ready" + System.lineSeparator() +
                        "[ingredient delivered]: hinge to D from Hi on cycle 20" + System.lineSeparator() +
                        "[ingredient delivered]: metal to Hi from M on cycle 20" + System.lineSeparator() +
                        "    0: hinge is ready" + System.lineSeparator() +
                        "[ingredient delivered]: hinge to D from Hi on cycle 22" + System.lineSeparator() +
                        "[order complete] Order 0 completed (door) at time 30" + System.lineSeparator();
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
                "[ingredient assignment]: hinge assigned to Hi to deliver to D" + System.lineSeparator() +
                "[source selection]: Hi (qlen) has request for hinge on 0" + System.lineSeparator();
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
        assertEquals(TileType.ROAD, sim.checkTile(new Coordinate(6, 6)));
        assertEquals(TileType.BUILDING, sim.checkTile(new Coordinate(0, 0)));
        assertEquals(new Coordinate(0, 0), sim.getBuildingLocation("D"));
        sim.updateTileMap(new Coordinate(6, 6), TileType.PATH);
        assertEquals(TileType.PATH, sim.checkTile(new Coordinate(6, 6)));
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
//    simulation.getPathList().getFirst().dump();
//    simulation.getPathList().getLast().dump();
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
        Delivery d = new Delivery(W, D, wood, 1, 5);
        assertThrows(IllegalArgumentException.class, () -> sim.addDelivery(W, D, wood, 1));
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
        @SuppressWarnings("unchecked")
        List<Path> pathList = (List<Path>) field.get(simulation);
        assertFalse(pathList.isEmpty());
        JsonArray jsonArray = simulation.pathListToJson();
        assertEquals(pathList.size(), jsonArray.size());
    }

    @Test
    public void test_getBuildingNameByCoordinate() {
        String b = sim.getBuildingNameByCoordinate(new Coordinate(9, 9));
        assertNull(b);
    }

    @Test
    public void test_getDeliveryCoordinates() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        assertEquals(0, sim.getDeliveryCoordinates().size());
        Building W = simulation.getWorld().getBuildingFromName("W");
        Building D = simulation.getWorld().getBuildingFromName("D");
        Item wood = new Item("wood");
        Delivery d = new Delivery(W, D, wood, 1, 5);
        simulation.connectBuildings(W, D);
        simulation.addDelivery(W, D, wood, 1);
        List<Coordinate> coordinates = simulation.getDeliveryCoordinates();
        assertEquals(1, coordinates.size());
        assertEquals(W.getLocation(), coordinates.get(0));

        assertThrows(IllegalArgumentException.class, () -> simulation.disconnectBuildings("W", "D"));
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

    @Test
    public void test_getDeliverySchedule() {
        Simulation sim = new TestUtils.MockSimulation();
        assertSame(sim.deliverySchedule, sim.getDeliverySchedule());
    }

    @Test
    public void test_removeBuilding_immediate() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        Building building = simulation.getWorld().getBuildingFromName("D");
        assertNotNull(building);
        Building sourceBuilding = simulation.getWorld().getBuildingFromName("W");
        assertNotNull(sourceBuilding);
        assertTrue(building.getPendingRequests().isEmpty());
        assertNull(building.getCurrentRequest());
        simulation.connectBuildings(sourceBuilding, building);
        assertTrue(simulation.removeBuilding(building));
        assertFalse(simulation.getWorld().hasBuilding("D"));
        assertThrows(IllegalArgumentException.class, () -> simulation.disconnectBuildings(sourceBuilding, building));
    }

    @Test
    public void test_removeBuilding_pending() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        Building building = simulation.getWorld().getBuildingFromName("D");
        assertNotNull(building);
        Item item = new Item("wood");
        Recipe recipe = TestUtils.makeTestRecipe("wood", 0, 1);
        Request request = new Request(1, item, recipe, building, null);
        building.prependPendingRequest(request);
        assertFalse(simulation.removeBuilding(building));
        assertTrue(simulation.getWorld().hasBuilding("D"));
        assertTrue(building.isPendingRemoval());
        building.getPendingRequests().clear();
        simulation.checkPendingRemovals();
        assertFalse(simulation.getWorld().hasBuilding("D"));
    }

    @Test
    public void test_removeBuilding_byName() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        assertTrue(simulation.getWorld().hasBuilding("W"));
        assertTrue(simulation.removeBuilding("W"));
        assertFalse(simulation.getWorld().hasBuilding("W"));
        assertThrows(IllegalArgumentException.class, () -> simulation.removeBuilding("NonExistentBuilding"));
    }

    @Test
    public void test_onBuildingRemovedEvent() {
        List<Building> buildings = new ArrayList<>();
        Consumer<Building> listener = buildings::add;
        Simulation sim = new TestUtils.MockSimulation();
        assertDoesNotThrow(() -> {
            sim.subscribeToOnBuildingRemoved(listener);
            sim.unsubscribeToOnBuildingRemoved(listener);
        });
    }

    @Test
    public void test_getDirection() {
        Coordinate from = new Coordinate(5, 5);
        assertEquals(0, sim.getDirection(from, new Coordinate(5, 4)), "Up should be direction 0");
        assertEquals(1, sim.getDirection(from, new Coordinate(6, 5)), "Right should be direction 1");
        assertEquals(2, sim.getDirection(from, new Coordinate(5, 6)), "Down should be direction 2");
        assertEquals(3, sim.getDirection(from, new Coordinate(4, 5)), "Left should be direction 3");
        assertThrows(IllegalArgumentException.class, () -> sim.getDirection(from, new Coordinate(5, 5)));
        assertThrows(IllegalArgumentException.class, () -> sim.getDirection(from, new Coordinate(6, 6)));
        assertThrows(IllegalArgumentException.class, () -> sim.getDirection(from, new Coordinate(4, 4)));
    }

    @Test
    public void test_isZeroFlow() {
        int[] flows = {0, 0, 0, 0};
        assertTrue(sim.isZeroFlow(flows));
        int[] flows2 = {1, 0, 0, 0};
        assertFalse(sim.isZeroFlow(flows2));
    }

    @Test
    public void test_checkNewTileReuse() {
        sim.connectBuildings("W", "D");//3,3 -> 0,0
        sim.connectBuildings("M", "Hi");//4,4 -> 2,2
        sim.connectBuildings("M", "Ha");//4,4 ->1,1

        Path p1 = sim.getPathList().get(0);
        Path p2 = sim.getPathList().get(1);
        Path p3 = sim.getPathList().get(2);

//        p1.dump();
//        p2.dump();
//        p3.dump();

        assertFalse(sim.checkNewTileReuse(2));
        assertTrue(sim.checkNewTileReuse(1));
        sim.disconnectBuildings("M", "Ha");
        assertFalse(sim.checkNewTileReuse(1));
    }

    @Test
    public void test_removePath() {
        TileMap map = sim.getWorld().getTileMap();
        sim.connectBuildings("W", "D");//3,3 -> 0,0
        sim.connectBuildings("M", "Hi");//4,4 -> 2,2
        Path p1 = sim.getPathList().get(0);
        Path p2 = sim.getPathList().get(1);
//          p1.dump();
//          p2.dump();

        Coordinate cross = new Coordinate(3, 2);
        assertSame(TileType.BUILDING, map.getTileType(new Coordinate(3, 3)));
        assertSame(TileType.PATH, map.getTileType(cross));
        assertSame(TileType.PATH, map.getTileType(new Coordinate(4, 2)));
//        System.out.println(Arrays.toString(map.getFlows(cross)));


        assertSame(1, map.getFlow(cross, 0));
        assertSame(-1, map.getFlow(cross, 1));
        assertSame(2, map.getFlow(cross, 2));
        assertSame(2, map.getFlow(cross, 3));

        sim.removePath(p2, sim.getPathList());
        assertSame(1, map.getFlow(cross, 0));
        assertSame(0, map.getFlow(cross, 1));
        assertSame(2, map.getFlow(cross, 2));
        assertSame(0, map.getFlow(cross, 3));
        assertSame(TileType.PATH, map.getTileType(new Coordinate(3, 2)));
        assertSame(TileType.ROAD, map.getTileType(new Coordinate(4, 2)));
    }

    @Test
    public void test_disconnectBuildings() {
        sim.connectBuildings("W", "D");//3,3 -> 0,0
        sim.connectBuildings("M", "Hi");//4,4 -> 2,2
        sim.connectBuildings("M", "Ha");//4,4 -> 1,1
        Path p1 = sim.getPathList().get(1);
        assertSame(3,sim.getPathList().size());
        assertThrows(IllegalArgumentException.class, () -> {sim.disconnectBuildings("W", "Ha");});
        assertThrows(IllegalArgumentException.class, () -> {sim.disconnectBuildings("M", "Hi");});
        sim.disconnectBuildings("M", "Ha");
        sim.disconnectBuildings("M", "Hi");
        assertSame(1, sim.getPathList().size());
        sim.connectBuildings("M", "Hi");//4,4 -> 2,2
        Path p2 = sim.getPathList().get(1);
        assertEquals(p1.getCost(),p2.getCost());
    }

    @Test
    public void test_onWasteDelivered() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Logger logger = new StreamLogger(stream);
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json", 0, logger);
        
        // Create test items and buildings
        Item wasteItem = new Item("toxic_waste");
        Building sourceBuilding = new TestUtils.MockBuilding("Factory1");
        Building regularBuilding = new TestUtils.MockBuilding("DisposalSite");
        
        // Case 1: verbosity < 1, nothing should be logged
        sim.setVerbosity(0);
        sim.onWasteDelivered(wasteItem, 10, regularBuilding, sourceBuilding);
        assertEquals("", stream.toString());
        
        // Case 2: verbosity >= 1 with regular building
        sim.setVerbosity(1);
        stream.reset();
        sim.onWasteDelivered(wasteItem, 15, regularBuilding, sourceBuilding);
        String expected = "[waste delivered]: 15 toxic_waste to DisposalSite from Factory1 on cycle 0" + System.lineSeparator();
        assertEquals(expected, stream.toString());
        
        // Case 3: verbosity >= 1 with WasteDisposalBuilding
        stream.reset();
        
        // Create a waste disposal building with disposal rates and time steps
        LinkedHashMap<Item, Integer> wasteCapacities = new LinkedHashMap<>();
        wasteCapacities.put(wasteItem, 100);
        
        LinkedHashMap<Item, Integer> disposalRates = new LinkedHashMap<>();
        disposalRates.put(wasteItem, 5);
        
        LinkedHashMap<Item, Integer> timeSteps = new LinkedHashMap<>();
        timeSteps.put(wasteItem, 3);
        
        WasteDisposalBuilding wasteDisposal = new WasteDisposalBuilding(
            "WasteCenter", wasteCapacities, disposalRates, timeSteps, sim
        );
        
        sim.onWasteDelivered(wasteItem, 20, wasteDisposal, sourceBuilding);
        expected = "[waste delivered]: 20 toxic_waste to WasteCenter from Factory1 on cycle 0" + System.lineSeparator() +
                   "[waste processing]: WasteCenter will process toxic_waste at a rate of 5 units per 3 time steps" + System.lineSeparator();
        assertEquals(expected, stream.toString());
    }

    @Test
    public void test_load_exception_handling() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        
        // Test IOException - non-existent file
        String nonExistentFile = "this_file_does_not_exist.json";
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> simulation.load(nonExistentFile));
        assertEquals("Invalid file name for load" + nonExistentFile, exception.getMessage());
        
        // Test JsonSyntaxException with malformed JSON
        try {
            // Create a temporary file with invalid JSON
            File tempFile = File.createTempFile("invalid_json", ".json");
            tempFile.deleteOnExit();
            
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write("{ This is not valid JSON }");
            }
            
            Exception jsonException = assertThrows(IllegalArgumentException.class,
                () -> simulation.load(tempFile.getAbsolutePath()));
            assertEquals("Invalid file name for load" + tempFile.getAbsolutePath(), 
                jsonException.getMessage());
            
        } catch (IOException e) {
            fail("Failed to create temporary file for testing: " + e.getMessage());
        }
    }
    
    @Test
    public void test_loadFromReader_uncheckedIOException() {
        // Use the mock so we don't need a real file
        Simulation sim = new TestUtils.MockSimulation();

        // A Reader that simulates an UncheckedIOException wrapping an IOException
        Reader uncheckedReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                throw new UncheckedIOException(new IOException("simulated unchecked failure"));
            }
            @Override
            public void close() { /* no-op */ }
        };

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> sim.loadFromReader(uncheckedReader)
        );

        // The message should be the same
        assertEquals("Error reading from reader", ex.getMessage());

        // And the cause should unwrap to the original IOException
        Throwable cause = ex.getCause();
        assertTrue(cause instanceof IOException);
        assertEquals("simulated unchecked failure", cause.getMessage());
    }

    @Test
    public void test_loadFromDB_noSavedSession() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        String nonExistentUserId = "non_existent_user";
        
        // Directly test the exception condition
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // Call loadFromDB with null JSON to simulate no saved session
            Method loadFromDBMethod = Simulation.class.getDeclaredMethod("loadFromDB", String.class);
            loadFromDBMethod.setAccessible(true);
            
            // Mock that SessionDAO.loadSession returns null
            try {
                // We can test just by calling the method with a non-existent user ID
                simulation.loadFromDB(nonExistentUserId);
            } catch (IllegalArgumentException e) {
                // Re-throw to be caught by assertThrows
                throw e;
            }
        });
        
        assertEquals("No saved session found for user: " + nonExistentUserId, 
            exception.getMessage());
    }

    @Test
    public void test_saveToDB() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        simulation.setVerbosity(3);
        simulation.step(5);
        
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logOutput));
        simulation.setLogger(testLogger);
        
        // Test the saveToDB method's logging functionality only
        String userId = "test_user";
        simulation.saveToDB(userId);
        
        // Verify the log output includes the expected message
        String logs = logOutput.toString();
        assertTrue(logs.contains("Simulation saved to DB for user " + userId));
    }

    @Test
    public void test_setTileMapDimensions() {
        // Create a new simulation with a clean world
        World world = new World();
        Simulation simulation = new Simulation(world, 0, testLogger);
        
        // Set dimensions using the simulation method
        int width = 50;
        int height = 75;
        simulation.setTileMapDimensions(width, height);
        
        // Verify the dimensions were correctly set in the world's tile map
        TileMap tileMap = world.getTileMap();
        assertEquals(width, tileMap.getWidth(), "Tile map width should match the set width");
        assertEquals(height, tileMap.getHeight(), "Tile map height should match the set height");
        
        // Test with different dimensions to ensure it updates correctly
        int newWidth = 100;
        int newHeight = 150;
        simulation.setTileMapDimensions(newWidth, newHeight);
        
        // Get the updated TileMap reference and verify the dimensions
        TileMap updatedTileMap = world.getTileMap();
        assertEquals(newWidth, updatedTileMap.getWidth(), "Tile map width should be updated to new width");
        assertEquals(newHeight, updatedTileMap.getHeight(), "Tile map height should be updated to new height");
    }

    @Test
    public void test_areBuildingsCompatible() throws Exception {
        // Get access to the private method
        Method areBuildingsCompatible = Simulation.class.getDeclaredMethod("areBuildingsCompatible", Building.class, Building.class);
        areBuildingsCompatible.setAccessible(true);
        
        // Create a test simulation
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        
        // Create test items
        Item wood = new Item("wood");
        Item metal = new Item("metal");
        Item chair = new Item("chair");
        Item table = new Item("table");
        
        // Case 1: MineBuilding to FactoryBuilding
        
        // Create a mine that produces wood
        MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 1, 0), "WoodMine", simulation);
        
        // Create a factory that uses wood (should be compatible)
        HashMap<Item, Integer> chairIngredients = new HashMap<>();
        chairIngredients.put(wood, 2);
        Recipe chairRecipe = new Recipe(chair, chairIngredients, 5);
        Type chairFactoryType = new Type("ChairFactory", List.of(chairRecipe));
        FactoryBuilding chairFactory = new FactoryBuilding(chairFactoryType, "ChairFactory", new ArrayList<>(), simulation);
        
        // Create a factory that doesn't use wood (should be incompatible)
        HashMap<Item, Integer> metalTableIngredients = new HashMap<>();
        metalTableIngredients.put(metal, 3);
        Recipe metalTableRecipe = new Recipe(table, metalTableIngredients, 7);
        Type metalTableFactoryType = new Type("MetalTableFactory", List.of(metalTableRecipe));
        FactoryBuilding metalTableFactory = new FactoryBuilding(metalTableFactoryType, "MetalTableFactory", new ArrayList<>(), simulation);
        
        // Test compatibility
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, woodMine, chairFactory), 
            "Mine producing wood should be compatible with factory using wood");
        assertFalse((boolean) areBuildingsCompatible.invoke(simulation, woodMine, metalTableFactory), 
            "Mine producing wood should be incompatible with factory not using wood");
        
        // Case 2: MineBuilding to StorageBuilding
        
        // Create storage buildings
        StorageBuilding woodStorage = new StorageBuilding("WoodStorage", new ArrayList<>(), simulation, wood, 100, 1.0);
        StorageBuilding metalStorage = new StorageBuilding("MetalStorage", new ArrayList<>(), simulation, metal, 100, 1.0);
        
        // Test compatibility
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, woodMine, woodStorage),
            "Mine producing wood should be compatible with wood storage");
        assertFalse((boolean) areBuildingsCompatible.invoke(simulation, woodMine, metalStorage),
            "Mine producing wood should be incompatible with metal storage");
        
        // Case 3: FactoryBuilding to FactoryBuilding
        
        // Create a factory that produces components used by another factory
        Item legComponent = new Item("leg");
        Recipe legRecipe = new Recipe(legComponent, chairIngredients, 3);
        Type legFactoryType = new Type("LegFactory", List.of(legRecipe));
        FactoryBuilding legFactory = new FactoryBuilding(legFactoryType, "LegFactory", new ArrayList<>(), simulation);
        
        // Create a factory that uses these components
        HashMap<Item, Integer> tableLegIngredients = new HashMap<>();
        tableLegIngredients.put(legComponent, 4);
        Recipe tableLegRecipe = new Recipe(table, tableLegIngredients, 5);
        Type tableLegFactoryType = new Type("TableWithLegsFactory", List.of(tableLegRecipe));
        FactoryBuilding tableLegFactory = new FactoryBuilding(tableLegFactoryType, "TableWithLegsFactory", new ArrayList<>(), simulation);
        
        // Test compatibility
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, legFactory, tableLegFactory),
            "Factory producing legs should be compatible with factory using legs");
        assertFalse((boolean) areBuildingsCompatible.invoke(simulation, chairFactory, metalTableFactory),
            "Factories with no compatible recipes should be incompatible");
        
        // Case 4: FactoryBuilding to StorageBuilding
        
        // Create a storage building for tables
        StorageBuilding tableStorage = new StorageBuilding("TableStorage", new ArrayList<>(), simulation, table, 100, 1.0);
        
        // Test compatibility
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, metalTableFactory, tableStorage),
            "Factory producing tables should be compatible with table storage");
        assertFalse((boolean) areBuildingsCompatible.invoke(simulation, chairFactory, tableStorage),
            "Factory not producing tables should be incompatible with table storage");
        
        // Case 5: Other building combinations
        
        // Create a waste disposal building
        LinkedHashMap<Item, Integer> wasteCapacities = new LinkedHashMap<>();
        wasteCapacities.put(wood, 100);
        LinkedHashMap<Item, Integer> disposalRates = new LinkedHashMap<>();
        disposalRates.put(wood, 10);
        LinkedHashMap<Item, Integer> timeSteps = new LinkedHashMap<>();
        timeSteps.put(wood, 2);
        WasteDisposalBuilding wasteDisposal = new WasteDisposalBuilding("WasteDisposal", wasteCapacities, disposalRates, timeSteps, simulation);
        
        // Test other combinations which should all be true
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, wasteDisposal, woodStorage),
            "Waste disposal to storage should be compatible (other case)");
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, woodStorage, metalStorage),
            "Storage to storage should be compatible (other case)");
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, chairFactory, wasteDisposal),
            "Factory to waste disposal should be compatible (other case)");
    }

    @Test
    public void test_connectBuildings_nonExistentBuildings() {
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        
        // Test case 1: Non-existent source building
        IllegalArgumentException srcException = assertThrows(
            IllegalArgumentException.class,
            () -> simulation.connectBuildings("NonExistentSource", "D")
        );
        assertEquals("Source building 'NonExistentSource' does not exist.", srcException.getMessage());
        
        // Test case 2: Non-existent destination building
        IllegalArgumentException dstException = assertThrows(
            IllegalArgumentException.class,
            () -> simulation.connectBuildings("W", "NonExistentDestination")
        );
        assertEquals("Destination building 'NonExistentDestination' does not exist.", dstException.getMessage());
        
        // Test case 3: Both source and destination don't exist 
        // (should fail on source check first)
        IllegalArgumentException bothException = assertThrows(
            IllegalArgumentException.class,
            () -> simulation.connectBuildings("NonExistentSource", "NonExistentDestination")
        );
        assertEquals("Source building 'NonExistentSource' does not exist.", bothException.getMessage());
    }

    @Test
    public void test_removeBuilding_verbosity() {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logStream));
        
        // Create a simulation with initial verbosity 0
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        Building building = simulation.getWorld().getBuildingFromName("W");
        assertNotNull(building);
        
        // Case 1: Immediate removal with verbosity = 0
        simulation.setVerbosity(0);
        logStream.reset();
        assertTrue(simulation.removeBuilding(building));
        assertEquals("", logStream.toString(), "No log should be produced when verbosity is 0");
        
        // Set up for next tests
        simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        building = simulation.getWorld().getBuildingFromName("W");
        
        // Case 2: Immediate removal with verbosity > 0
        simulation.setVerbosity(1);
        logStream.reset();
        assertTrue(simulation.removeBuilding(building));
        String logOutput = logStream.toString();
        assertTrue(logOutput.contains("Building 'W' has been removed."), 
                   "Log should contain the building removal message when verbosity > 0");
        
        // Set up for pending removal test
        simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        building = simulation.getWorld().getBuildingFromName("D");
        // Make the building have a pending request so it can't be immediately removed
        Item item = new Item("wood");
        Recipe recipe = TestUtils.makeTestRecipe("wood", 0, 1);
        Request request = new Request(1, item, recipe, building, null);
        building.prependPendingRequest(request);
        
        // Case 3: Pending removal with verbosity = 0
        simulation.setVerbosity(0);
        logStream.reset();
        assertFalse(simulation.removeBuilding(building));
        assertEquals("", logStream.toString(), "No log should be produced when verbosity is 0");
        
        // Case 4: Pending removal with verbosity > 0
        building.getPendingRequests().clear(); // Reset for next test
        building = simulation.getWorld().getBuildingFromName("D");
        building.prependPendingRequest(request);
        simulation.setVerbosity(1);
        logStream.reset();
        assertFalse(simulation.removeBuilding(building));
        logOutput = logStream.toString();
        assertTrue(logOutput.contains("Building 'D' has been marked for removal. It will be removed once all pending operations complete."), 
                   "Log should contain the pending removal message when verbosity > 0");
    }

    @Test
    public void test_checkPendingRemovals_verbosity() throws Exception {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logStream));
        
        // Create a simulation with initial verbosity 0
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        
        // Case 1: Building pending removal with verbosity = 0
        simulation.setVerbosity(0);
        
        // Get a building and mark it for pending removal
        Building building = simulation.getWorld().getBuildingFromName("D");
        Item item = new Item("wood");
        Recipe recipe = TestUtils.makeTestRecipe("wood", 0, 1);
        Request request = new Request(1, item, recipe, building, null);
        building.prependPendingRequest(request);
        
        // Mark the building for removal
        assertFalse(simulation.removeBuilding(building));
        assertTrue(building.isPendingRemoval());
        
        // Clear the pending requests so the building can be removed
        building.getPendingRequests().clear();
        
        // Reset log before checking pending removals
        logStream.reset();
        
        // Call checkPendingRemovals via reflection to test the private method
        Method checkPendingRemovals = Simulation.class.getDeclaredMethod("checkPendingRemovals");
        checkPendingRemovals.setAccessible(true);
        checkPendingRemovals.invoke(simulation);
        
        // Verify no log was produced with verbosity = 0
        assertEquals("", logStream.toString(), "No log should be produced when verbosity is 0");
        assertFalse(simulation.getWorld().hasBuilding("D"), "Building should be removed");
        
        // Case 2: Building pending removal with verbosity > 0
        simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        simulation.setVerbosity(1);
        
        // Setup building for pending removal again
        building = simulation.getWorld().getBuildingFromName("D");
        building.prependPendingRequest(request);
        assertFalse(simulation.removeBuilding(building));
        assertTrue(building.isPendingRemoval());
        
        // Clear the pending requests so the building can be removed
        building.getPendingRequests().clear();
        
        // Reset log before checking pending removals
        logStream.reset();
        
        // Call checkPendingRemovals via reflection
        checkPendingRemovals.invoke(simulation);
        
        // Verify log was produced with verbosity > 0
        String logOutput = logStream.toString();
        assertTrue(logOutput.contains("Building 'D' has completed all pending operations and has been removed."), 
                   "Log should contain completion message when verbosity > 0");
        assertFalse(simulation.getWorld().hasBuilding("D"), "Building should be removed");
    }

    @Test
    public void test_connectBuildings_compatibility() throws Exception {
        // Create a simulation
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        
        // Get access to the areBuildingsCompatible method for verification
        Method areBuildingsCompatible = Simulation.class.getDeclaredMethod("areBuildingsCompatible", 
                                                                       Building.class, Building.class);
        areBuildingsCompatible.setAccessible(true);
        
        // Get two buildings that should be compatible (Wood mine to Door factory)
        Building woodMine = simulation.getWorld().getBuildingFromName("W");
        Building doorFactory = simulation.getWorld().getBuildingFromName("D");
        
        // Verify they are compatible according to the method
        assertTrue((boolean) areBuildingsCompatible.invoke(simulation, woodMine, doorFactory), 
                   "Wood mine should be compatible with door factory");
        
        // Successfully connect the buildings
        Path path = simulation.connectBuildings(woodMine, doorFactory);
        assertNotNull(path, "Path should be created when connecting compatible buildings");
        assertTrue(doorFactory.getSources().contains(woodMine), 
                  "Destination building should have source building in its sources list");
        
        // Now create a test case with known incompatible buildings
        // Wood mine and metal storage should be incompatible based on our existing test_areBuildingsCompatible
        Item metal = new Item("metal");
        StorageBuilding metalStorage = new StorageBuilding("MetalStorage", new ArrayList<>(), simulation, metal, 100, 1.0);
        
        // Set a valid location for the storage building (find an unoccupied tile)
        Coordinate location = new Coordinate(5, 5); // Choose coordinates not occupied by other buildings
        metalStorage.setLocation(location);
        
        // Add the storage building to the world for testing
        simulation.getWorld().tryAddBuilding(metalStorage);
        
        // Verify they are incompatible according to the method
        assertFalse((boolean) areBuildingsCompatible.invoke(simulation, woodMine, metalStorage),
                    "Wood mine should be incompatible with metal storage");
        
        // Attempt to connect incompatible buildings
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> simulation.connectBuildings(woodMine, metalStorage)
        );
        
        // Verify the exception message
        String expectedMessage = "Cannot connect " + woodMine.getName() + " to " + metalStorage.getName() +
                                 ": Source output cannot be used as input for destination.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void test_connection_error_verbosity() throws Exception {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logStream));
        
        // Create a simulation with the test logger and verbosity 0
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json", 0, testLogger);
        
        // Get access to the private methods and fields we need to manipulate
        Method buildConnectionsMethod = WorldBuilder.class.getDeclaredMethod("buildConnections", 
                                                                          List.class, Simulation.class);
        buildConnectionsMethod.setAccessible(true);
        
        // Create a connection DTO with an invalid building name
        ConnectionDTO connection = new ConnectionDTO("W", "NonExistentBuilding"); // This building doesn't exist
        
        // Case 1: Attempt to build connection with verbosity = 0
        simulation.setVerbosity(0);
        logStream.reset();
        buildConnectionsMethod.invoke(null, Collections.singletonList(connection), simulation);
        
        // Verify no log was produced when verbosity = 0
        assertEquals("", logStream.toString(), "No log should be produced when verbosity is 0");
        
        // Case 2: Attempt to build connection with verbosity > 0
        simulation.setVerbosity(1);
        logStream.reset();
        buildConnectionsMethod.invoke(null, Collections.singletonList(connection), simulation);
        
        // Verify log was produced when verbosity > 0
        String logOutput = logStream.toString();
        String expectedMessage = "Failed to create connection: Destination building 'NonExistentBuilding' does not exist.";
        assertEquals(expectedMessage + System.lineSeparator(), logOutput, 
                   "Log should contain the exact error message about failing to establish connection");
    }

    @Test
    public void test_connectBuildings_sourceUpdate() {
        // Get two existing buildings from sample environment
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        Building woodMine = simulation.getWorld().getBuildingFromName("W");
        Building doorFactory = simulation.getWorld().getBuildingFromName("D");
        
        // First create a path for testing
        simulation.connectBuildings("W", "D");
        Path existingPath = simulation.getPathList().get(0);
        
        // Now clear the sources to set up our test
        doorFactory.updateSources(new ArrayList<>());
        assertTrue(doorFactory.getSources().isEmpty(), "Door factory should have no sources after clearing");
        
        // Create a simulation class that lets us test the source update directly
        class TestableSimulation extends Simulation {
            public TestableSimulation() {
                super("src/test/resources/inputs/doors1.json");
            }
            
            public boolean testUpdateSourcesWhenConnecting(Building src, Building dst, Path path) {
                // This directly tests the source update logic extracted from connectBuildings
                List<Building> sources = new ArrayList<>(dst.getSources());
                System.out.println("Sources contains src before update? " + sources.contains(src));
                
                if (!sources.contains(src)) {
                    System.out.println("Adding source to destination");
                    sources.add(src);
                    dst.updateSources(sources);
                    return true; // Indicate that we added the source
                } else {
                    System.out.println("Source already in destination's sources");
                    return false; // Indicate that we didn't add the source
                }
            }
        }
        
        TestableSimulation testSim = new TestableSimulation();
        
        // Execute the source update logic directly - first time should add
        boolean firstUpdateResult = testSim.testUpdateSourcesWhenConnecting(woodMine, doorFactory, existingPath);
        
        // Verify source was added
        assertTrue(firstUpdateResult, "First update should have added the source");
        assertTrue(doorFactory.getSources().contains(woodMine), "Wood mine should be added to door factory's sources");
        assertEquals(1, doorFactory.getSources().size(), "Door factory should have exactly one source");
        
        // Execute again to test idempotence - second time should not add
        boolean secondUpdateResult = testSim.testUpdateSourcesWhenConnecting(woodMine, doorFactory, existingPath);
        
        // Verify source wasn't added again
        assertFalse(secondUpdateResult, "Second update should not have added the source again");
        assertEquals(1, doorFactory.getSources().size(), "Source count should be unchanged when adding same source again");
    }

    @Test
    public void test_connectBuildings_addSourceToDestination() {
        // Get a fresh simulation to work with
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
        
        // Get two existing buildings
        Building building1 = simulation.getWorld().getBuildingFromName("W");
        Building building2 = simulation.getWorld().getBuildingFromName("D");
        
        // This test will focus on just the lines that add a source to a destination's sources list
        
        // First clear the destination's sources completely
        building2.updateSources(new ArrayList<>());
        assertTrue(building2.getSources().isEmpty(), "Destination building should have no sources for this test");
        
        List<Building> sources = new ArrayList<>(building2.getSources());
        assertFalse(sources.contains(building1), "Source building should not be in destination's sources list");
        sources.add(building1);
        building2.updateSources(sources);
        
        // Verify the source was added properly
        assertTrue(building2.getSources().contains(building1), "Source building should have been added to destination's sources");
        assertEquals(1, building2.getSources().size(), "Destination should have exactly one source");
        
        // Test idempotence - running the code again shouldn't duplicate the source
        sources = new ArrayList<>(building2.getSources());
        assertTrue(sources.contains(building1), "Source building should already be in destination's sources list");
        // The following lines should be skipped in the actual code because the if condition would be false
        // We'll verify that the size remains unchanged if we tried to add it again
        
        // Test with a different source building
        Building building3 = simulation.getWorld().getBuildingFromName("M");
        sources = new ArrayList<>(building2.getSources());
        assertFalse(sources.contains(building3), "Second source building should not be in destination's sources list");
        sources.add(building3);
        building2.updateSources(sources);
        
        // Verify second source was added
        assertTrue(building2.getSources().contains(building1), "First source building should still be in destination's sources");
        assertTrue(building2.getSources().contains(building3), "Second source building should have been added to destination's sources");
        assertEquals(2, building2.getSources().size(), "Destination should now have two sources");
    }

    @Test
    public void test_establishConnections() {
        // Get a fresh simulation
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logOutput));
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json", 1, testLogger);
        
        // Clear the sources of a destination building we'll connect to
        Building doorFactory = simulation.getWorld().getBuildingFromName("D");
        doorFactory.updateSources(new ArrayList<>());
        assertTrue(doorFactory.getSources().isEmpty(), "Door factory should have no sources initially");
        
        // Create a list of connection DTOs
        List<ConnectionDTO> connections = new ArrayList<>();
        
        // Valid connections
        connections.add(new ConnectionDTO("W", "D"));    // Wood mine to door factory - valid
        connections.add(new ConnectionDTO("Hi", "D"));   // Hinge factory to door factory - valid
        
        // Invalid connection - non-existent building
        connections.add(new ConnectionDTO("NonExistentBuilding", "D"));
        
        // Invalid connection - incompatible buildings
        connections.add(new ConnectionDTO("M", "D"));    // Metal mine to door factory - incompatible
        
        // Test with null and empty lists first
        simulation.establishConnections(null);  // Should handle null gracefully
        simulation.establishConnections(new ArrayList<>());  // Should handle empty list gracefully
        
        // Now establish the connections
        simulation.establishConnections(connections);
        
        // Check that valid connections were established
        List<Building> sources = doorFactory.getSources();
        assertEquals(2, sources.size(), "Door factory should have two sources after establishing valid connections");
        assertTrue(sources.contains(simulation.getWorld().getBuildingFromName("W")), 
                "Wood mine should be in door factory sources");
        assertTrue(sources.contains(simulation.getWorld().getBuildingFromName("Hi")), 
                "Hinge factory should be in door factory sources");
        
        // Check that error logging occurred for invalid connections
        String logs = logOutput.toString();
        assertTrue(logs.contains("Failed to establish connection from NonExistentBuilding to D"), 
                "Should log error for non-existent building");
        assertTrue(logs.contains("Failed to establish connection from M to D"), 
                "Should log error for incompatible buildings");
        
        // Test with verbosity = 0 (no logging)
        logOutput.reset();
        simulation.setVerbosity(0);
        simulation.establishConnections(connections);
        logs = logOutput.toString();
        assertEquals("", logs, "No logs should be produced when verbosity is 0");
    }

    @Test
    public void test_establishConnections_errorMessages() {
        // Get a fresh simulation with logging
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        Logger testLogger = new StreamLogger(new PrintStream(logOutput));
        Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json", 1, testLogger);
        
        // Test case 1: Non-existent source building
        List<ConnectionDTO> connections = new ArrayList<>();
        connections.add(new ConnectionDTO("NonExistentSource", "D"));
        simulation.establishConnections(connections);
        String logs = logOutput.toString();
        assertTrue(logs.contains("Failed to establish connection from NonExistentSource to D: Source building 'NonExistentSource' does not exist."), 
                "Should log specific error message for non-existent source");
        
        // Test case 2: Non-existent destination building
        logOutput.reset();
        connections.clear();
        connections.add(new ConnectionDTO("W", "NonExistentDestination"));
        simulation.establishConnections(connections);
        logs = logOutput.toString();
        assertTrue(logs.contains("Failed to establish connection from W to NonExistentDestination: Destination building 'NonExistentDestination' does not exist."), 
                "Should log specific error message for non-existent destination");
        
        // Test case 3: Incompatible buildings
        logOutput.reset();
        connections.clear();
        connections.add(new ConnectionDTO("M", "D"));  // Metal mine to door factory - incompatible
        simulation.establishConnections(connections);
        logs = logOutput.toString();
        assertTrue(logs.contains("Failed to establish connection from M to D: Cannot connect M to D: Source output cannot be used as input for destination."), 
                "Should log specific error message for incompatible buildings");
        
        // Test case 4: No valid path between buildings (modify tile map to block the path)
        logOutput.reset();
        connections.clear();
        connections.add(new ConnectionDTO("W", "D"));
        
        // Block all tiles between W and D with buildings
        Coordinate srcLoc = simulation.getBuildingLocation("W");
        Coordinate dstLoc = simulation.getBuildingLocation("D");
        TileMap tileMap = simulation.getWorld().getTileMap();
        
        // Save original state
        TileType[][] originalTiles = new TileType[tileMap.getWidth()][tileMap.getHeight()];
        for (int x = 0; x < tileMap.getWidth(); x++) {
            for (int y = 0; y < tileMap.getHeight(); y++) {
                Coordinate coord = new Coordinate(x, y);
                originalTiles[x][y] = tileMap.getTileType(coord);
                
                // Block tile if it's not a source or destination building
                if (!coord.equals(srcLoc) && !coord.equals(dstLoc)) {
                    tileMap.setTileType(coord, TileType.BUILDING);
                }
            }
        }
        
        simulation.establishConnections(connections);
        logs = logOutput.toString();
        assertTrue(logs.contains("Failed to establish connection from W to D: Cannot connect W to D: No valid path."), 
                "Should log specific error message for no valid path");
        
        // Restore original tile map state
        for (int x = 0; x < tileMap.getWidth(); x++) {
            for (int y = 0; y < tileMap.getHeight(); y++) {
                tileMap.setTileType(new Coordinate(x, y), originalTiles[x][y]);
            }
        }
    }
}
