package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class RecipeTest {
  @Test
  public void test_constructor_and_getters() {
    // build the necessary items
    Item wood = new Item("wood");
    Item handle = new Item("handle");
    Item hinge = new Item("hinge");
    Item door = new Item("door");

    // build the ingredients hashmap for constructor
    HashMap<Item, Integer> ingredients = new HashMap<>();
    ingredients.put(wood, 1);
    ingredients.put(handle, 2);
    ingredients.put(hinge, 3);

    // constructs the recipe
    Recipe doorRecipe = new Recipe(door, ingredients, 12);

    // test the ingredients
    HashMap<Item, Integer> doorIngredients = doorRecipe.getIngredients();
    assertTrue(doorIngredients.containsKey(wood));
    assertTrue(doorIngredients.containsKey(handle));
    assertTrue(doorIngredients.containsKey(hinge));
    assertFalse(doorIngredients.containsKey(door));
    assertEquals(1, doorIngredients.get(wood));
    assertEquals(2, doorIngredients.get(handle));
    assertEquals(3, doorIngredients.get(hinge));

    // test output and latency
    assertSame(door, doorRecipe.getOutput());
    assertEquals(12, doorRecipe.getLatency());
  }
}
