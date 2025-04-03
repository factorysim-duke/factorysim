package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TypeTest {

    private Type dollAndBottle;
    private Recipe dollRecipe;
    private Recipe bottleRecipe;

    @BeforeEach
    public void setUp() {
      Item cotton = new Item("cotton");
      Item doll = new Item("doll");
      Item plastic = new Item("plastic");
      Item bottle = new Item("bottle");

      HashMap<Item, Integer> dollIngredients = new HashMap<>();
      dollIngredients.put(cotton, 10);
      HashMap<Item, Integer> bottleIngredients = new HashMap<>();
      bottleIngredients.put(plastic, 5);

      this.dollRecipe = new Recipe(doll, dollIngredients, 6);
      this.bottleRecipe = new Recipe(bottle, bottleIngredients, 20);

      ArrayList<Recipe> recipes = new ArrayList<>();
      recipes.add(dollRecipe);
      recipes.add(bottleRecipe);
      this.dollAndBottle = new Type("DBFactory", recipes);
    }

    @Test
    public void test_constructor_and_getters() {
      assertEquals("DBFactory", dollAndBottle.getName());
      assertNotNull(dollAndBottle.getRecipes());
      assertEquals(2, dollAndBottle.getRecipes().size());
      assertSame(dollRecipe, dollAndBottle.getRecipes().get(0));
      assertSame(bottleRecipe, dollAndBottle.getRecipes().get(1));
    }

    @Test
    public void test_toJson() {
      JsonObject json = dollAndBottle.toJson();

      assertEquals("DBFactory", json.get("name").getAsString());

      JsonArray recipesArray = json.getAsJsonArray("recipes");
      assertEquals(2, recipesArray.size());
      assertEquals("doll", recipesArray.get(0).getAsString());
      assertEquals("bottle", recipesArray.get(1).getAsString());
    }

  @Test
  public void test_invalid_names() {
    ArrayList<Recipe> emptyRecipe = new ArrayList<>();
    assertThrows(IllegalArgumentException.class, () -> new Type("'", emptyRecipe));
    assertThrows(IllegalArgumentException.class, () -> new Type("Do'or", emptyRecipe));
  }

}
