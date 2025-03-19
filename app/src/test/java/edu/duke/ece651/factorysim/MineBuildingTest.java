package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MineBuildingTest {
  @Test
  public void test_constructor_and_getters() {
    Item out = new Item("out");
    Recipe recipe = new Recipe(out, new HashMap<>(), 6);
    MineBuilding mine = new MineBuilding(recipe, "myMine", new TestUtils.MockSimulation());
    assertSame(out, mine.getResource());
    assertEquals(6, mine.getMiningLatency());
    assertEquals("myMine", mine.getName());
    assertSame(recipe, mine.getMiningRecipe());
  }

  @Test
  public void test_processRequestEasyVersion() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 3, 2);
    MineBuilding mine = new MineBuilding(woodRecipe, "woodMine", new TestUtils.MockSimulation());

    FactoryBuilding factory = new FactoryBuilding(new Type("Chair Factory",
        new ArrayList<>()), "Chair Factory", List.of(mine), new TestUtils.MockSimulation());

    // Mine shouldn't be processing any request now
    assertFalse(mine.isProcessing());

    // Add request
    Request request = new Request(1, wood, woodRecipe, mine, factory);
    mine.addRequest(request);

    // Mine shouldn't be finished since there's a pending request
    assertFalse(mine.isFinished());

    // Request is not complete initially because recipe latency is 3
    assertFalse(request.isCompleted());

    // Process once
    mine.processRequestEasyVersion(); // Request remaining steps = 2
    assertFalse(request.isCompleted()); // Request should be incomplete
    assertTrue(mine.isProcessing()); // Mine should be processing the request
    assertFalse(mine.isFinished()); // Mine should not be finished since it's processing the request

    // Process once again
    mine.processRequestEasyVersion(); // Request remaining steps = 1
    assertFalse(request.isCompleted()); // Request should be incomplete
    assertTrue(mine.isProcessing()); // Mine should still be processing the request
    assertFalse(mine.isFinished()); // Mine should not be finished since it's processing the request

    // Process once again again
    mine.processRequestEasyVersion(); // Request remaining steps = 0
    assertTrue(request.isCompleted()); // Request should be completed now
    assertFalse(mine.isProcessing()); // Mine shouldn't be processing now

    // Mine should be finished now since there's no current and pending requests
    assertTrue(mine.isFinished());

    // This `processRequestEasyVersion` should not change anything since there's no pending
    // requests
    mine.processRequestEasyVersion();
    assertTrue(request.isCompleted());
    assertFalse(mine.isProcessing());
  }

  @Test
  public void test_has_all_ingredients_and_consume() {
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 1, 2); // the ingredients should be {("a", 1), ("b", 2)}
    Building building = new TestUtils.MockBuilding("MockBuilding");
    assertFalse(building.hasAllIngredientsFor(testRecipe));
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    building.addToStorage(a, 2);
    assertFalse(building.hasAllIngredientsFor(testRecipe));
    building.addToStorage(b, 1);
    assertFalse(building.hasAllIngredientsFor(testRecipe));
    building.addToStorage(b, 1);
    assertTrue(building.hasAllIngredientsFor(testRecipe));
    building.addToStorage(c, 1);
    assertTrue(building.hasAllIngredientsFor(testRecipe));

    building.consumeIngredientsFor(testRecipe);
    assertFalse(building.hasAllIngredientsFor(testRecipe));
    assertEquals(1, building.getStorageNumberOf(a));
    assertEquals(-1, building.getStorageNumberOf(b));
    assertEquals(1, building.getStorageNumberOf(c));

    HashMap<Item, Integer> missingIngredients = building.findMissingIngredients(testRecipe);
    assertTrue(missingIngredients.keySet().contains(b));
    assertFalse(missingIngredients.keySet().contains(a));
    assertFalse(missingIngredients.keySet().contains(c));
    assertEquals(2, missingIngredients.get(b));
  }

  @Test
  public void test_find_missing_ingredients() {
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 1, 4); // the ingredients should be {("a", 1), ("b", 2), ("c", 3), ("d", 4)}
    Building building = new TestUtils.MockBuilding("MockBuilding");
    assertFalse(building.hasAllIngredientsFor(testRecipe));
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    Item d = new Item("d");
    building.addToStorage(a, 1);
    building.addToStorage(c, 4);
    building.addToStorage(d, 3);
    
    HashMap<Item, Integer> missingIngredients = building.findMissingIngredients(testRecipe);
    assertTrue(missingIngredients.keySet().contains(b));
    assertFalse(missingIngredients.keySet().contains(a));
    assertFalse(missingIngredients.keySet().contains(c));
    assertTrue(missingIngredients.keySet().contains(d));
    assertEquals(2, missingIngredients.get(b));
    assertEquals(1, missingIngredients.get(d));
  }
}
