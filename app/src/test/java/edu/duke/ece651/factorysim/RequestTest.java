package edu.duke.ece651.factorysim;

import java.util.*;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
  @Test
  public void test_constructor_and_getters() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 2); // ingredients should be {(a: 1), (b: 2)}
    Building mine = new MineBuilding(woodRecipe, "woodMine", new TestUtils.MockSimulation());
    List<Building> sources = new ArrayList<>();
    sources.add(mine);
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 3);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(doorRecipe);
    Type type = new Type("door", recipes);
    Building factory = new FactoryBuilding(type, "doorFactory", sources, new TestUtils.MockSimulation());

    // user request wood from mine
    int orderNum1 = 1;
    Request request1 = new Request(orderNum1, wood, woodRecipe, mine, null);
    assertEquals(1, request1.getOrderNum());
    assertSame(woodRecipe, request1.getRecipe());
    assertSame(wood, request1.getItem());
    assertSame(mine, request1.getProducer());
    assertNull(request1.getDeliverTo());
    assertTrue(request1.isUserRequest());

    // factory request wood from mine
    int orderNum2 = 2;
    Request request2 = new Request(orderNum2, door, doorRecipe, mine, factory);
    assertEquals(2, request2.getOrderNum());
    assertSame(doorRecipe, request2.getRecipe());
    assertSame(door, request2.getItem());
    assertSame(mine, request2.getProducer());
    assertSame(factory, request2.getDeliverTo());
    assertFalse(request2.isUserRequest());
  }

  @Test
  public void test_process_isCompleted() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 3, 2);
    Building mine = new MineBuilding(woodRecipe, "woodMine", new TestUtils.MockSimulation());

    Request r = new Request(1, wood, woodRecipe, mine, null);
    assertEquals(r.getStatus(),"pending");
    assertFalse(r.process()); // remainingSteps = 2
    assertFalse(r.isCompleted());

    assertFalse(r.process()); // remainingSteps = 1
    assertFalse(r.isCompleted());

    assertTrue(r.process()); // remainingSteps = 0
    assertTrue(r.isCompleted());

    assertTrue(r.process()); // remainingSteps = 0
    assertTrue(r.isCompleted());

    int orderNum = 1;
    Request request = new Request(orderNum, wood, woodRecipe, mine, null);

    assertEquals(request.getStatus(), "pending");

    request.setStatus("current");
    assertEquals("current", request.getStatus());

    request.setStatus("completed");
    assertEquals("completed", request.getStatus());

    request.setRemainingSteps(0);
    assertEquals(0, request.getRemainingSteps());
  }

  @Test
  public void test_toJson() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 2);
    Building mine = new MineBuilding(woodRecipe, "woodMine", new TestUtils.MockSimulation());

    Request request = new Request(1, wood, woodRecipe, mine, null);

    JsonObject json = request.toJson();

    assertEquals(1, json.get("orderNum").getAsInt());
    assertEquals("wood", json.get("item").getAsString());
    assertEquals("wood", json.get("recipe").getAsString());
    assertEquals("woodMine", json.get("producer").getAsString());
    assertEquals("null", json.get("deliverTo").getAsString());
    assertEquals(request.getRemainingSteps(), json.get("remainingSteps").getAsInt());
    assertEquals(request.getStatus(), json.get("status").getAsString());
  }

  @Test
  public void test_toJson_withDeliverTo() {
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 3);
    Building mine = new MineBuilding(doorRecipe, "woodMine", new TestUtils.MockSimulation());
    Building factory = new FactoryBuilding(new Type("door", List.of(doorRecipe)), "doorFactory", List.of(mine), new TestUtils.MockSimulation());

    Request request = new Request(2, door, doorRecipe, mine, factory);

    JsonObject json = request.toJson();

    assertEquals(2, json.get("orderNum").getAsInt());
    assertEquals("door", json.get("item").getAsString());
    assertEquals("door", json.get("recipe").getAsString());
    assertEquals("woodMine", json.get("producer").getAsString());
    assertEquals("doorFactory", json.get("deliverTo").getAsString());
    assertEquals(request.getRemainingSteps(), json.get("remainingSteps").getAsInt());
    assertEquals(request.getStatus(), json.get("status").getAsString());
  }

}
