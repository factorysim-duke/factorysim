package edu.duke.ece651.factorysim;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TestUtils {
  /**
   * Conviniently generates a hashmap for ingredients.
   * Used in recipe constructor and thus in type, building, ...
   * The form will be like {("a", 1), ("b", 2), ("c", 3), ...}
   * 
   * @param num is the number of key-value pairs you want in the map. Please make
   *            it between 1-25.
   * @return the generated ingredients map used for testing.
   */
  public static HashMap<Item, Integer> makeTestIngredientMap(int num) {
    char start = 'a';
    HashMap<Item, Integer> ans = new HashMap<>();
    for (int i = 0; i < num; i++) {
      char cur = (char) (start + i);
      String name = String.valueOf(cur);
      Item item = new Item(name);
      ans.put(item, i + 1);
    }
    return ans;
  }

  /**
   * Conveniently generates a recipe for testing.
   * 
   * @param itemName         is the String name of the recipe's output item.
   * @param latency          is the latency for the recipe.
   * @param numOfIngredients is the number of ingredients you want in the
   *                         ingredients hashmap (in the form of {("a", 1), ("b",
   *                         2)...} as described in makeTestIngredients())
   * @return the recipe used for tesing.
   */
  public static Recipe makeTestRecipe(String itemName, int latency, int numOfIngredients) {
    HashMap<Item, Integer> ingredients = makeTestIngredientMap(numOfIngredients);
    Item item = new Item(itemName);
    Recipe ans = new Recipe(item, ingredients, latency);
    return ans;
  }

  /**
   * Loads the config data from the given file path.
   * 
   * @param filePath is the path to the config data file.
   * @return the config data.
   */
  public static ConfigData loadConfigData(String filePath) {
    try {
      String json = new String(Files.readAllBytes(Paths.get(filePath)));
      return new Gson().fromJson(json, ConfigData.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load config data from " + filePath, e);
    }
  }

  /**
   * Mock simulation for testing.
   */
  public static class MockSimulation extends Simulation {
    public MockSimulation() {
      super("src/test/resources/inputs/doors1.json");
    }

    @Override
    public RequestPolicy getRequestPolicy(String building) {
      return new FifoRequestPolicy();
    }
  }

  /**
   * Mock building for testing.
   */
  public static class MockBuilding extends Building {
    public MockBuilding(String name) {
      super(name, new ArrayList<>(), new MockSimulation());
    }

    @Override
    public boolean canProduce(Item item) {
      return true;
    }

    @Override
    public JsonObject toJson() {
      return null;
    }

    @Override
    public boolean canBeRemovedImmediately() {
      return true;
    }
  }
}
