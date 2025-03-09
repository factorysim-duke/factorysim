package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class TypeTest {
  @Test
  public void test_constructor_and_getters() {
    Item cotton = new Item("cotton");
    Item doll = new Item("doll");
    Item plastic = new Item("plastic");
    Item bottle = new Item("bottle");
    HashMap<Item, Integer> dollIngredients = new HashMap<>();
    dollIngredients.put(cotton, 10);
    HashMap<Item, Integer> bottleIngredients = new HashMap<>();
    bottleIngredients.put(plastic, 5);
    Recipe dollRecipe = new Recipe(doll, dollIngredients, 6);
    Recipe bottleRecipe = new Recipe(bottle, bottleIngredients, 20);
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(dollRecipe);
    recipes.add(bottleRecipe);
    Type dollAndBottle = new Type("DBFactory", recipes);

    assertEquals("DBFactory", dollAndBottle.getName());
    assertNotNull(dollAndBottle.getRecipes());
    assertEquals(2, dollAndBottle.getRecipes().size());
    assertSame(dollRecipe, dollAndBottle.getRecipes().get(0));
    assertSame(bottleRecipe, dollAndBottle.getRecipes().get(1));
  }

}
