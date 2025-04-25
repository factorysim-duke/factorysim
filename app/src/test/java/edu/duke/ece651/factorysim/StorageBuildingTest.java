package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StorageBuildingTest {
  Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

  public StorageBuilding makeTestStorageBuilding(String name, Item item, int capacity, double priority) {
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    Recipe recipeA = new Recipe(a, new HashMap<>(), 1);
    Recipe recipeB = new Recipe(b, new HashMap<>(), 3);
    Recipe recipeC = new Recipe(c, new HashMap<>(), 5);
    MineBuilding mineA = new MineBuilding(recipeA, "mineA", sim);
    MineBuilding mineB = new MineBuilding(recipeB, "mineB", sim);
    MineBuilding mineC = new MineBuilding(recipeC, "mineC", sim);
    ArrayList<Building> sources = new ArrayList<>();
    sources.add(mineA);
    sources.add(mineB);
    sources.add(mineC);
    return new StorageBuilding(name, sources, sim, item, capacity, priority);
  }

  @Test
  public void test_constructor_and_getters() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    assertSame(door, testBuilding.getStorageItem());
    assertEquals(100, testBuilding.getMaxCapacity());
    assertEquals(0.5, testBuilding.getPriority());
    assertEquals(0, testBuilding.getArrivingItemNum());
    assertEquals(0, testBuilding.getCurrentStockNum());
  }

  @Test
  public void test_add_to_and_take_from_storage() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    Item random = new Item("random");
    assertTrue(testBuilding.canProduce(door));
    assertFalse(testBuilding.canProduce(random));
    assertThrows(IllegalArgumentException.class, () -> testBuilding.addToStorage(random, 10));
    assertThrows(IllegalArgumentException.class, () -> testBuilding.addToStorage(door, 101));
    testBuilding.addToStorage(door, 99);
    assertEquals(99, testBuilding.getArrivingItemNum());
    assertEquals(0, testBuilding.getCurrentStockNum());
    testBuilding.step();
    testBuilding.step();
    testBuilding.step();
    assertEquals(0, testBuilding.getArrivingItemNum());
    assertEquals(99, testBuilding.getCurrentStockNum());
    assertThrows(IllegalArgumentException.class, () -> testBuilding.takeFromStorage(random, 10));
    assertThrows(IllegalArgumentException.class, () -> testBuilding.takeFromStorage(door, 100));
    testBuilding.takeFromStorage(door, 98);
    assertEquals(0, testBuilding.getArrivingItemNum());
    assertEquals(1, testBuilding.getCurrentStockNum());
    assertEquals(1, testBuilding.getStorageNumberOf(door));
    assertEquals(-1, testBuilding.getStorageNumberOf(random));
  }

  @Test
  public void test_step_ingredient_delivery() {
    Item door = new Item("door");

    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    FactoryBuilding destinationBuilding = new FactoryBuilding(new Type("DoorFactory", List.of()), "doorFactory",
        List.of(testBuilding), sim);
    testBuilding.setLocation(new Coordinate(50, 0));
    destinationBuilding.setLocation(new Coordinate(60, 2));
    sim.getWorld().tryAddBuilding(testBuilding);
    sim.getWorld().tryAddBuilding(destinationBuilding);

    testBuilding.addToStorage(door, 10);
    testBuilding.step();
    assertEquals(10, testBuilding.getCurrentStockNum());

    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 0, 1);
    Request ingredientRequest = new Request(2, door, doorRecipe, testBuilding, destinationBuilding);
    testBuilding.getPendingRequest().add(ingredientRequest);
    sim.connectBuildings(testBuilding, destinationBuilding);
    testBuilding.step();
    assertEquals(9, testBuilding.getCurrentStockNum());
    assertEquals(-1, destinationBuilding.getStorageNumberOf(door));
  }

  @Test
  public void test_step_insufficient_stock() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 0, 1);
    Request request1 = new Request(1, door, doorRecipe, testBuilding, null);
    Request request2 = new Request(2, door, doorRecipe, testBuilding, null);
    testBuilding.getPendingRequest().add(request1);
    testBuilding.getPendingRequest().add(request2);
    testBuilding.addToStorage(door, 1);
    testBuilding.step();
    testBuilding.step();
    assertEquals(0, testBuilding.getCurrentStockNum());
    assertEquals(1, testBuilding.getPendingRequest().size());
  }

  @Test
  public void test_step_refill_with_available_source() {
    Simulation simulation = new TestUtils.MockSimulation();
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 2, 5);
    Recipe handleRecipe = TestUtils.makeTestRecipe("handle", 2, 5);
    Recipe hingeRecipe = TestUtils.makeTestRecipe("hinge", 2, 5);
    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 0, 1), "woodMine", simulation);
    MineBuilding metalMine = new MineBuilding(TestUtils.makeTestRecipe("metal", 0, 1), "metalMine", simulation);
    Type handleType = new Type("HandleFactory", List.of(handleRecipe));
    FactoryBuilding handleFactory = new FactoryBuilding(handleType, "handleFactory", List.of(woodMine, metalMine),
        simulation);
    Type hingeType = new Type("HingeFactory", List.of(hingeRecipe));
    FactoryBuilding hingeFactory = new FactoryBuilding(hingeType, "hingeFactory", List.of(woodMine, metalMine),
        simulation);
    Type doorType = new Type("DoorFactory", List.of(doorRecipe));
    FactoryBuilding doorFactory = new FactoryBuilding(doorType, "doorFactory",
        List.of(woodMine, metalMine, handleFactory, hingeFactory), simulation);

    ArrayList<Building> sources = new ArrayList<>();
    sources.add(doorFactory);
    StorageBuilding testBuilding = new StorageBuilding("test", sources, simulation, door, 100, 1.0);

    testBuilding.addToStorage(door, 10);
    testBuilding.step();
    assertEquals(10, testBuilding.getCurrentStockNum());
    testBuilding.step();
  }

  @Test
  public void test_step_R_less_than_or_equal_to_zero() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    testBuilding.addToStorage(door, 100);
    testBuilding.step();
    assertEquals(100, testBuilding.getCurrentStockNum());
    testBuilding.step();
  }

  @Test
  public void test_step_currentTime_mod_F_not_zero() {
    Item door = new Item("door");
    final int[] currentTimeValue = { 1 };
    TestUtils.MockSimulation mockSim = new TestUtils.MockSimulation() {
      @Override
      public int getCurrentTime() {
        return currentTimeValue[0];
      }
    };
    StorageBuilding testBuilding = new StorageBuilding("test", new ArrayList<>(), mockSim, door, 10, 0.5);
    testBuilding.addToStorage(door, 5);
    testBuilding.step();
    assertEquals(5, testBuilding.getCurrentStockNum());

    currentTimeValue[0] = 41; // 41 % 40 = 1 != 0
    testBuilding.step();
  }

  @Test
  public void test_toJson() {
    Item door = new Item("door");
    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 0, 1), "woodMine",
        new TestUtils.MockSimulation());
    MineBuilding metalMine = new MineBuilding(TestUtils.makeTestRecipe("metal", 0, 1), "metalMine",
        new TestUtils.MockSimulation());
    StorageBuilding doorStorage = new StorageBuilding("doorStorage", List.of(woodMine, metalMine),
        new TestUtils.MockSimulation(), door, 100, 0.75);
    doorStorage.setLocation(new Coordinate(10, 20));
    JsonObject json = doorStorage.toJson();

    assertEquals("doorStorage", json.get("name").getAsString());
    assertEquals("door", json.get("stores").getAsString());
    assertEquals(100, json.get("capacity").getAsInt());
    assertEquals(0.75, json.get("priority").getAsDouble());

    JsonArray sourcesArray = json.getAsJsonArray("sources");
    assertNotNull(sourcesArray);
    assertEquals(2, sourcesArray.size());
    assertEquals("woodMine", sourcesArray.get(0).getAsString());
    assertEquals("metalMine", sourcesArray.get(1).getAsString());

    JsonObject storageJson = json.getAsJsonObject("storage");
    assertNotNull(storageJson);
    assertEquals(0, storageJson.entrySet().size());
    assertEquals(10, json.get("x").getAsInt());
    assertEquals(20, json.get("y").getAsInt());

    doorStorage.addToStorage(door, 5);
    doorStorage.step();
    json = doorStorage.toJson();
    storageJson = json.getAsJsonObject("storage");
    assertNotNull(storageJson);
    assertTrue(storageJson.has("door"));
    assertEquals(5, storageJson.get("door").getAsInt());
  }

  @Test
  public void test_toJson_empty_storage() {
    Item metal = new Item("metal");
    StorageBuilding metalStorage = new StorageBuilding("metalStorage", List.of(),
        new TestUtils.MockSimulation(), metal, 50, 0.5);
    JsonObject json = metalStorage.toJson();

    assertEquals("metalStorage", json.get("name").getAsString());
    assertEquals("metal", json.get("stores").getAsString());
    assertEquals(50, json.get("capacity").getAsInt());
    assertEquals(0.5, json.get("priority").getAsDouble());

    JsonArray sourcesArray = json.getAsJsonArray("sources");
    assertNotNull(sourcesArray);
    assertEquals(0, sourcesArray.size());
    JsonObject storageJson = json.getAsJsonObject("storage");
    assertNotNull(storageJson);
    assertEquals(0, storageJson.entrySet().size());
  }

  @Test
  public void testGetNumOfPendingRequests_withStock() {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    Item door = new Item("door");
    StorageBuilding sb = new StorageBuilding("test", new ArrayList<>(), sim, door, 100, 0.5);
    sb.addToStorage(door, 10);
    sb.step();
    assertEquals(-10, sb.getNumOfPendingRequests());
  }

  @Test
  public void testGetNumOfPendingRequests_noStock() {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    Item door = new Item("door");
    StorageBuilding sb = new StorageBuilding("test", new ArrayList<>(), sim, door, 100, 0.5);
    Request r1 = new Request(1, door, sim.getRecipeForItem(door), sb, null);
    Request r2 = new Request(2, door, sim.getRecipeForItem(door), sb, null);
    sb.getPendingRequests().add(r1);
    sb.getPendingRequests().add(r2);
    assertEquals(2, sb.getNumOfPendingRequests());
  }

  @Test
  public void testSumRemainingLatencies_withStock() {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    Item door = new Item("door");
    StorageBuilding sb = new StorageBuilding("test", new ArrayList<>(), sim, door, 100, 0.5);
    sb.addToStorage(door, 10);
    sb.step();
    int recipeLatency = 1;
    recipeLatency = sim.getRecipeForItem(door).getLatency();

    int expected = -(recipeLatency * 10);
    assertEquals(expected, sb.sumRemainingLatencies());
  }

  @Test
  public void testSumRemainingLatencies_noStock() {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    Item door = new Item("door");
    StorageBuilding sb = new StorageBuilding("test", new ArrayList<>(), sim, door, 100, 0.5);
    Request r = new Request(1, door, sim.getRecipeForItem(door), sb, null);
    sb.getPendingRequests().add(r);
    assertEquals(r.getRemainingSteps(), sb.sumRemainingLatencies());
  }

  @Test
  public void testGetNumOfPendingRequests_stockOverridesPending() {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    Item door = new Item("door");
    StorageBuilding sb = new StorageBuilding("test", new ArrayList<>(), sim, door, 100, 0.5);
    Request r1 = new Request(1, door, sim.getRecipeForItem(door), sb, null);
    sb.getPendingRequests().add(r1);
    sb.addToStorage(door, 5);
    sb.step();
    assertEquals(-5, sb.getNumOfPendingRequests());
  }

  @Test
  public void test_canBeRemovedImmediately() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    assertTrue(testBuilding.canBeRemovedImmediately());
    testBuilding.addToStorage(door, 1);
    testBuilding.step();
    assertFalse(testBuilding.canBeRemovedImmediately());
    testBuilding.takeFromStorage(door, 1);
    assertTrue(testBuilding.canBeRemovedImmediately());
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 0, 1);
    Request request = new Request(1, door, doorRecipe, testBuilding, null);
    testBuilding.prependPendingRequest(request);
    assertFalse(testBuilding.canBeRemovedImmediately());
    testBuilding.getPendingRequests().clear();
    assertTrue(testBuilding.canBeRemovedImmediately());
    testBuilding.setCurrentRequest(request);
    assertFalse(testBuilding.canBeRemovedImmediately());
  }

  @Test
  public void test_canAcceptRequest() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 0, 1);
    FactoryBuilding factoryBuilding = new FactoryBuilding(new Type("DoorFactory", List.of()), "doorFactory",
        List.of(testBuilding), sim);
    Request takeRequest = new Request(1, door, doorRecipe, testBuilding, factoryBuilding);
    Request storeRequest = new Request(2, door, doorRecipe, factoryBuilding, testBuilding);
    assertTrue(testBuilding.canAcceptRequest(takeRequest));
    assertTrue(testBuilding.canAcceptRequest(storeRequest));
    testBuilding.addToStorage(door, 1);
    testBuilding.step();
    assertFalse(testBuilding.markForRemoval());
    assertTrue(testBuilding.isPendingRemoval());
    assertTrue(testBuilding.canAcceptRequest(takeRequest));
    assertFalse(testBuilding.canAcceptRequest(storeRequest));
  }

  @Test
  public void test_markForRemoval() throws NoSuchFieldException, IllegalAccessException {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    assertFalse(testBuilding.isPendingRemoval());
    assertTrue(testBuilding.markForRemoval());
    testBuilding.addToStorage(door, 1);
    testBuilding.step();

    java.lang.reflect.Field pendingRemovalField = Building.class.getDeclaredField("pendingRemoval");
    pendingRemovalField.setAccessible(true);
    pendingRemovalField.set(testBuilding, false);

    assertFalse(testBuilding.markForRemoval());
    assertTrue(testBuilding.isPendingRemoval());
  }

}
