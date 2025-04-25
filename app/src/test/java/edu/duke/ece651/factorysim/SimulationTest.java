package edu.duke.ece651.factorysim;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import edu.duke.ece651.factorysim.db.SessionDAO;

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
}
