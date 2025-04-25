package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

public class FactoryBuildingTest {
  private Type makeTestFactoryType() {
    // {("a", 1), ("b", 2)}
    HashMap<Item, Integer> ingredients1 = TestUtils.makeTestIngredientMap(2);
    // {("a", 1), ("b", 2), ("c", 3)}
    HashMap<Item, Integer> ingredients2 = TestUtils.makeTestIngredientMap(3);
    Item out1 = new Item("out1");
    Item out2 = new Item("out2");
    Recipe recipe1 = new Recipe(out1, ingredients1, 10);
    Recipe recipe2 = new Recipe(out2, ingredients2, 20);
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe1);
    recipes.add(recipe2);

    Type factoryType = new Type("ABCFactory", recipes);
    return factoryType;
  }

  @Test
  public void test_getters_and_canProduce() {
    Item a = new Item("a");
    Item out1 = new Item("out1");
    Item out2 = new Item("out2");
    Type factoryType = makeTestFactoryType();
    FactoryBuilding factory = new FactoryBuilding(factoryType, "myFactory", new ArrayList<>(),
        new TestUtils.MockSimulation());

    assertEquals(30, factory.getRemainingLatency());
    assertEquals("myFactory", factory.getName());
    assertTrue(factory.canProduce(out1));
    assertTrue(factory.canProduce(out2));
    assertFalse(factory.canProduce(a));
    assertEquals(0, factory.getSources().size());
  }

  @Test
  public void test_invalid_name() {
    Type factoryType = makeTestFactoryType();
    assertThrows(IllegalArgumentException.class,
        () -> new FactoryBuilding(factoryType, "'factory", new ArrayList<>(), new TestUtils.MockSimulation()));
    assertThrows(IllegalArgumentException.class,
        () -> new FactoryBuilding(factoryType, "Fac tory'", new ArrayList<>(), new TestUtils.MockSimulation()));
  }

  @Test
  public void test_interaction_with_mine() {
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    Recipe recipeA = new Recipe(a, new HashMap<>(), 1);
    Recipe recipeB = new Recipe(b, new HashMap<>(), 3);
    Recipe recipeC = new Recipe(c, new HashMap<>(), 5);
    MineBuilding mineA = new MineBuilding(recipeA, "mineA", new TestUtils.MockSimulation());
    MineBuilding mineB = new MineBuilding(recipeB, "mineB", new TestUtils.MockSimulation());
    MineBuilding mineC = new MineBuilding(recipeC, "mineC", new TestUtils.MockSimulation());

    Type factoryType = makeTestFactoryType();
    ArrayList<Building> sources = new ArrayList<>();
    sources.add(mineA);
    sources.add(mineB);
    sources.add(mineC);
    FactoryBuilding factory = new FactoryBuilding(factoryType, "myFactory", sources, new TestUtils.MockSimulation());

    List<Building> factorySources = factory.getSources();
    assertEquals(3, factorySources.size());
    assertEquals("mineA", factorySources.get(0).getName());
    assertEquals("mineB", factorySources.get(1).getName());
    assertEquals("mineC", factorySources.get(2).getName());

    Item d = new Item("d");
    assertEquals(-1, factory.getStorageNumberOf(d));
    assertEquals(-1, factory.getStorageNumberOf(a));

    mineA.deliverTo(factory, a, 2, false);
    assertEquals(2, factory.getStorageNumberOf(a));
    assertEquals(-1, factory.getStorageNumberOf(b));
    assertEquals(-1, factory.getStorageNumberOf(c));

    mineC.deliverTo(factory, c, 3, false);
    assertEquals(3, factory.getStorageNumberOf(c));

    factory.takeFromStorage(a, 2);
    factory.takeFromStorage(c, 2);
    assertEquals(1, factory.getStorageNumberOf(c));
    assertEquals(-1, factory.getStorageNumberOf(a));

    mineB.deliverTo(factory, b, 4, false);
    mineB.deliverTo(factory, b, 10, false);
    assertEquals(14, factory.getStorageNumberOf(b));

    assertThrows(IllegalArgumentException.class, () -> factory.takeFromStorage(d, 3));
    assertThrows(IllegalArgumentException.class, () -> factory.takeFromStorage(c, 2));
    assertThrows(IllegalArgumentException.class, () -> factory.takeFromStorage(a, 1));
  }

  @Test
  public void test_toJson() {
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 2, 5);
    Type factoryType = new Type("DoorFactory", List.of(doorRecipe));

    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 0, 1), "woodMine",
        new TestUtils.MockSimulation());
    MineBuilding metalMine = new MineBuilding(TestUtils.makeTestRecipe("metal", 0, 1), "metalMine",
        new TestUtils.MockSimulation());

    FactoryBuilding doorFactory = new FactoryBuilding(factoryType, "doorFactory", List.of(woodMine, metalMine),
        new TestUtils.MockSimulation());
    doorFactory.addToStorage(door, 3);

    JsonObject json = doorFactory.toJson();
    assertEquals("doorFactory", json.get("name").getAsString());
    assertEquals("DoorFactory", json.get("type").getAsString());
    assertEquals(2, json.getAsJsonArray("sources").size());
    assertEquals("woodMine", json.getAsJsonArray("sources").get(0).getAsString());
    assertEquals("metalMine", json.getAsJsonArray("sources").get(1).getAsString());
    assertEquals(3, json.getAsJsonObject("storage").get("door").getAsInt());
  }

  @Test
  public void test_request_in_building() {
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 2, 5);
    Type factoryType = new Type("DoorFactory", List.of(doorRecipe));

    MineBuilding woodMine = new MineBuilding(TestUtils.makeTestRecipe("wood", 0, 1), "woodMine",
        new TestUtils.MockSimulation());
    MineBuilding metalMine = new MineBuilding(TestUtils.makeTestRecipe("metal", 0, 1), "metalMine",
        new TestUtils.MockSimulation());

    FactoryBuilding doorFactory = new FactoryBuilding(factoryType, "doorFactory", List.of(woodMine, metalMine),
        new TestUtils.MockSimulation());

    Request request1 = new Request(1, door, doorRecipe, doorFactory, null);
    Request request2 = new Request(2, door, doorRecipe, doorFactory, null);

    doorFactory.getPendingRequest().add(request1);
    doorFactory.getPendingRequest().add(request2);
    doorFactory.processRequestEasyVersion();

    assertEquals(1, doorFactory.getPendingRequest().size());
    assertSame(request1, doorFactory.getCurrentRequest());

    doorFactory.setCurrentRequest(request2);
    assertEquals(doorFactory.getCurrentRequest(), request2);
  }

  @Test
  public void test_get_recipe_for_item() {
    Building mockBuilding = new TestUtils.MockBuilding("D");
    Recipe doorRecipe = mockBuilding.getRecipeForItem(new Item("door"));
    assertNotNull(doorRecipe);
    Recipe nullRecipe = mockBuilding.getRecipeForItem(new Item("notExist"));
    assertNull(nullRecipe);
  }

  @Test
  public void test_request_missing_ingredients_from_missing_source() {
    Recipe nonExistingRecipe = TestUtils.makeTestRecipe("test", 2, 5);
    Building mockBuilding = new TestUtils.MockBuilding("D");
    assertThrows(IllegalArgumentException.class, () -> mockBuilding.requestMissingIngredients(nonExistingRecipe));
  }

  @Test
  public void test_canBeRemovedImmediately() {
    FactoryBuilding testBuilding = makeTestFactoryBuilding("test", new Item("testItem"));
    assertTrue(testBuilding.canBeRemovedImmediately());
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 0, 1);
    Request request = new Request(1, new Item("testItem"), testRecipe, testBuilding, null);
    testBuilding.prependPendingRequest(request);
    assertFalse(testBuilding.canBeRemovedImmediately());
    testBuilding.getPendingRequests().clear();
    assertTrue(testBuilding.canBeRemovedImmediately());
    testBuilding.setCurrentRequest(request);
    assertFalse(testBuilding.canBeRemovedImmediately());
  }

  @Test
  public void test_canAcceptRequest() {
    FactoryBuilding testBuilding = makeTestFactoryBuilding("test", new Item("testItem"));
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 0, 1);
    Request request = new Request(1, new Item("testItem"), testRecipe, testBuilding, null);
    assertTrue(testBuilding.canAcceptRequest(request));
    testBuilding.prependPendingRequest(request);
    assertFalse(testBuilding.markForRemoval());
    assertTrue(testBuilding.isPendingRemoval());
    assertFalse(testBuilding.canAcceptRequest(request));
  }

  @Test
  public void test_markForRemoval() throws NoSuchFieldException, IllegalAccessException {
    FactoryBuilding testBuilding = makeTestFactoryBuilding("test", new Item("testItem"));
    assertFalse(testBuilding.isPendingRemoval());
    assertTrue(testBuilding.markForRemoval());
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 0, 1);
    Request request = new Request(1, new Item("testItem"), testRecipe, testBuilding, null);
    testBuilding.prependPendingRequest(request);
    
    java.lang.reflect.Field pendingRemovalField = Building.class.getDeclaredField("pendingRemoval");
    pendingRemovalField.setAccessible(true);
    pendingRemovalField.set(testBuilding, false);

    assertFalse(testBuilding.markForRemoval());
    assertTrue(testBuilding.isPendingRemoval());
  }

  @Test
  public void test_addRequest_whenBuildingMarkedForRemoval() {
    // Create factory building
    FactoryBuilding testBuilding = makeTestFactoryBuilding("test_factory", new Item("testItem"));
    Recipe testRecipe = TestUtils.makeTestRecipe("testItem", 0, 1);
    
    // Add a request to prevent immediate removal
    Request pendingRequest = new Request(1, new Item("testItem"), testRecipe, testBuilding, null);
    testBuilding.prependPendingRequest(pendingRequest);
    
    // Mark the building for removal - should return false since it has a pending request
    assertFalse(testBuilding.markForRemoval());
    assertTrue(testBuilding.isPendingRemoval());
    
    // Create a new request to attempt to add
    Request newRequest = new Request(2, new Item("testItem"), testRecipe, testBuilding, null);
    
    // Test that addRequest throws IllegalArgumentException because building is marked for removal
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      testBuilding.addRequest(newRequest);
    });
    
    // Verify the exception message
    String expectedMessage = "Building test_factory is marked for removal and cannot accept this request";
    assertEquals(expectedMessage, exception.getMessage());
  }

  private FactoryBuilding makeTestFactoryBuilding(String name, Item item) {
    Recipe recipe = TestUtils.makeTestRecipe(item.getName(), 0, 1);
    Type factoryType = new Type(item.getName() + "Factory", List.of(recipe));
    return new FactoryBuilding(factoryType, name, new ArrayList<>(), new TestUtils.MockSimulation());
  }
}
