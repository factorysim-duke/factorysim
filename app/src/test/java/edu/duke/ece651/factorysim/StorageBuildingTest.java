package edu.duke.ece651.factorysim;

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
  public void test_add_to_storage() {
    Item door = new Item("door");
    StorageBuilding testBuilding = makeTestStorageBuilding("test", door, 100, 0.5);
    
  }

}
