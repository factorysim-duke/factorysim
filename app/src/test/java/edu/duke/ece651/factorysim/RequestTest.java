package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
  @Test
  public void test_constructor_and_getters() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 2); // ingredients should be {(a: 1), (b: 2)}
    Building mine = new MineBuilding(woodRecipe, "woodMine");
    List<Building> sources = new ArrayList<>();
    sources.add(mine);
    Item door = new Item("door");
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 3);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(doorRecipe);
    Type type = new Type("door", recipes);
    Building factory = new FactoryBuilding(type, "doorFactory", sources);

    // user request wood from mine
    int orderNum1 = 1;
    Request request1 = new Request(orderNum1, wood, woodRecipe, mine, null, true);
    assertEquals(1, request1.getOrderNum());
    assertSame(woodRecipe, request1.getRecipe());
    assertSame(wood, request1.getItem());
    assertSame(mine, request1.getProducer());
    assertNull(request1.getDeliverTo());
    assertTrue(request1.isUserRequest());

    // factory request wood from mine
    int orderNum2 = 2;
    Request request2 = new Request(orderNum2, door, doorRecipe, mine, factory, false);
    assertEquals(2, request2.getOrderNum());
    assertSame(doorRecipe, request2.getRecipe());
    assertSame(door, request2.getItem());
    assertSame(mine, request2.getProducer());
    assertSame(factory, request2.getDeliverTo());
    assertFalse(request2.isUserRequest());
  }

  @Test
  public void test_process() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 3, 2);
    Building mine = new MineBuilding(woodRecipe, "woodMine");

    Request r1 = new Request(1, wood, woodRecipe, mine, null, true);
    assertFalse(r1.process()); // remainingSteps = 2
    assertFalse(r1.process()); // remainingSteps = 1
    assertTrue(r1.process()); // remainingSteps = 0
    assertTrue(r1.process()); // remainingSteps = 0
  }
}
