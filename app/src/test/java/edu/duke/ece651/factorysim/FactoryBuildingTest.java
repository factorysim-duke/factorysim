package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class FactoryBuildingTest {
  @Test
  public void test_getters_and_canProduce() {
    Item a = new Item("a");
    Item b = new Item("b");
    Item c = new Item("c");
    Item out1 = new Item("out1");
    Item out2 = new Item("out2");

    // {("a", 1), ("b", 2)}
    HashMap<Item, Integer> ingredients1 = TestUtils.makeTestIngredientMap(2);
    // {("a", 1), ("b", 2), ("c", 3)}
    HashMap<Item, Integer> ingredients2 = TestUtils.makeTestIngredientMap(3);
    Recipe recipe1 = new Recipe(out1, ingredients1, 10);
    Recipe recipe2 = new Recipe(out2, ingredients2, 20);
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe1);
    recipes.add(recipe2);

    Type factoryType = new Type("ABCFactory", recipes);
    FactoryBuilding factory = new FactoryBuilding(factoryType, "myFactory", new ArrayList<>());

    assertEquals(30, factory.getRemainingLatency());
    assertEquals("myFactory", factory.getName());
    assertTrue(factory.canProduce(out1));
    assertTrue(factory.canProduce(out2));
    assertFalse(factory.canProduce(a));
  }
}
