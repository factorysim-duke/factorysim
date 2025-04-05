package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

public class StorageBuildingTest {
  public StorageBuilding makeTestStorageBuilding(String name, Item item, int capacity, double priority) {
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    Recipe recipeA = new Recipe(a, new HashMap<>(), 1);
    Recipe recipeB = new Recipe(b, new HashMap<>(), 3);
    Recipe recipeC = new Recipe(c, new HashMap<>(), 5);
    MineBuilding mineA = new MineBuilding(recipeA, "mineA", new TestUtils.MockSimulation());
    MineBuilding mineB = new MineBuilding(recipeB, "mineB", new TestUtils.MockSimulation());
    MineBuilding mineC = new MineBuilding(recipeC, "mineC", new TestUtils.MockSimulation());
    ArrayList<Building> sources = new ArrayList<>();
    sources.add(mineA);
    sources.add(mineB);
    sources.add(mineC);
    return new StorageBuilding(name, sources, new TestUtils.MockSimulation(), item, capacity, priority);
  }

  @Test
  public void test_constructor_and_getters() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    assertSame(door, testBuilding.getStorageItem());
    assertEquals(100, testBuilding.getMaxCapacity());
    assertEquals(0.5, testBuilding.getPriority());
    assertEquals(0, testBuilding.getOutstandingRequestNum());
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
  }

  @Test
  public void test_step_ingredient_delivery() {
    Item door = new Item("door");
    TestUtils.MockSimulation mockSim = new TestUtils.MockSimulation();
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    FactoryBuilding destinationBuilding = new FactoryBuilding(new Type("DoorFactory", List.of()), "doorFactory",
        List.of(testBuilding), mockSim);
    testBuilding.addToStorage(door, 10);
    testBuilding.step();
    assertEquals(10, testBuilding.getCurrentStockNum());

    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 0, 1);
    Request ingredientRequest = new Request(2, door, doorRecipe, testBuilding, destinationBuilding);
    testBuilding.getPendingRequest().add(ingredientRequest);
    testBuilding.step();
    assertEquals(9, testBuilding.getCurrentStockNum());
    assertEquals(0, testBuilding.getPendingRequest().size());
    assertEquals(1, destinationBuilding.getStorageNumberOf(door));
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
}
