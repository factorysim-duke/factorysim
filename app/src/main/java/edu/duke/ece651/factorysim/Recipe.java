package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a recipe in the simulation.
 */
public class Recipe {
  private final Item output;
  private final LinkedHashMap<Item, Integer> ingredients;
  private final int latency;
  private final LinkedHashMap<Item, Integer> wasteByProducts;

  /**
   * Constructs a recipe.
   *
   * @param output      is the item being created by the recipe.
   * @param ingredients is the hashmap of ingredients required by the recipe.
   * @param latency     is the integer value for how many time steps it takes
   *                    for
   *                    a building to perform this recipe.
   */
  public Recipe(Item output, HashMap<Item, Integer> ingredients, int latency) {
    this(output, new LinkedHashMap<>(ingredients), latency, new LinkedHashMap<>());
  }

  /**
   * Constructs a recipe.
   *
   * @param output          is the item being created by the recipe.
   * @param ingredients     is the hashmap of ingredients required by the recipe.
   * @param latency         is the integer value for how many time steps it takes
   *                        for
   *                        a building to perform this recipe.
   * @param wasteByProducts is the hashmap of waste byproducts created by the
   *                        recipe.
   */
  public Recipe(Item output, HashMap<Item, Integer> ingredients, int latency, HashMap<Item, Integer> wasteByProducts) {
    this(output, new LinkedHashMap<>(ingredients), latency, new LinkedHashMap<>(wasteByProducts));
  }

  /**
   * Constructs a recipe.
   *
   * @param output          is the item being created by the recipe.
   * @param ingredients     is the linked hashmap of ingredients required by the
   *                        recipe.
   * @param latency         is the integer value for how many time steps it takes
   *                        for
   *                        a building to perform this recipe.
   * @param wasteByProducts is the hashmap of waste byproducts created by the
   *                        recipe.
   */
  public Recipe(Item output, LinkedHashMap<Item, Integer> ingredients, int latency,
      LinkedHashMap<Item, Integer> wasteByProducts) {
    this.output = output;
    this.ingredients = ingredients;
    this.latency = latency;
    this.wasteByProducts = wasteByProducts;
  }

  /**
   * Gets the output of the recipe.
   *
   * @return the output item of the recipe.
   */
  public Item getOutput() {
    return output;
  }

  /**
   * Gets the ingredients of the recipe.
   *
   * @return the hashmap of ingredients of the recipe.
   */
  public LinkedHashMap<Item, Integer> getIngredients() {
    return ingredients;
  }

  /**
   * Gets the waste byproducts of the recipe.
   *
   * @return the hashmap of waste byproducts of the recipe.
   */
  public LinkedHashMap<Item, Integer> getWasteByProducts() {
    return wasteByProducts;
  }

  /**
   * Gets the latency of the recipe.
   * 
   * @return the latency it takes for a building to perform this recipe.
   */
  public int getLatency() {
    return latency;
  }

  /**
   * Checks if the recipe produces waste byproducts.
   *
   * @return true if the recipe produces waste, false otherwise.
   */
  public boolean hasWasteByProducts() {
    return !wasteByProducts.isEmpty();
  }

  /**
   * Converts the Recipe object to a JSON representation.
   *
   * @return a JsonObject representing the recipe.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("output", output.getName());
    JsonObject ingredientsJson = new JsonObject();
    for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
      ingredientsJson.addProperty(entry.getKey().getName(), entry.getValue());
    }
    json.add("ingredients", ingredientsJson);
    json.addProperty("latency", latency);
    if (hasWasteByProducts()) {
      JsonObject wasteJson = new JsonObject();
      for (Map.Entry<Item, Integer> entry : wasteByProducts.entrySet()) {
        wasteJson.addProperty(entry.getKey().getName(), entry.getValue());
      }
      json.add("waste", wasteJson);
    }
    return json;
  }
}
