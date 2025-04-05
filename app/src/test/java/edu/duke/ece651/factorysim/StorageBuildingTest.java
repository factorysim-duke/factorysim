package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;

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
  public void test_can_produce(){
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    assertTrue(testBuilding.canProduce(door));
    Item random = new Item("random");
    assertFalse(testBuilding.canProduce(random));
  }
}
