package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a mine building in the simulation.
 */
public class MineBuilding extends Building {
  private final Recipe miningRecipe;
  private final Item resource; // got from miningRecipe
  private final int remainingLatency; // got from miningRecipe

  /**
   * Constructs a basic mine with empty storage and no ingredient source
   * buildings.
   *
   * @param miningRecipe is the recipe for this mine.
   * @param name         is the name of the building.
   * @param simulation   is the injected simulation instance.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public MineBuilding(Recipe miningRecipe, String name, Simulation simulation) {
    super(name, new ArrayList<>(), simulation);
    this.miningRecipe = miningRecipe;
    this.resource = miningRecipe.getOutput();
    this.remainingLatency = miningRecipe.getLatency();
  }

  /**
   * Gets the resource of this mine.
   * 
   * @return the resource item of this mine.
   */
  public Item getResource() {
    return resource;
  }

  /**
   * Gets the mining latency of this mine (i.e. how many time steps to consume to
   * mine 1 resource item)
   * 
   * @return the mining latency of this mine.
   */
  public int getMiningLatency() {
    return remainingLatency;
  }

  /**
   * Gets the mining recipe of this mine.
   * 
   * @return the mining recipe of this mine.
   */
  public Recipe getMiningRecipe() {
    return miningRecipe;
  }

  /**
   * Checks if this mine can produce a given item.
   * 
   * @param item is the item to be checked.
   * @return true if this mine can produce this item, false otherwise.
   */
  public boolean canProduce(Item item) {
    if (miningRecipe.getOutput().getName().equals(item.getName())) {
      return true;
    }
    return false;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getName());
    json.addProperty("mine", miningRecipe.getOutput().getName());
    JsonArray sourcesArray = new JsonArray();
    json.add("sources", sourcesArray);
    JsonObject storage = new JsonObject();
    if (!getStorage().isEmpty()) {
      for (Map.Entry<Item, Integer> entry : getStorage().entrySet()) {
        storage.addProperty(entry.getKey().getName(), entry.getValue());
      }
    }
    json.add("storage", storage);

    // added for evolution 2: adapt to location
    if (this.getLocation() != null) {
      json.addProperty("x", this.getLocation().getX());
      json.addProperty("y", this.getLocation().getY());
    }
    return json;
  }

  @Override
  public boolean hasAllIngredientsFor(Recipe recipe) {
    return canProduce(recipe.getOutput());
  }

  /**
   * Checks if this mine building can be removed immediately.
   * A mine building can be removed immediately if it has no requests in its
   * queue.
   *
   * @return true if the building can be removed immediately, false otherwise.
   */
  @Override
  public boolean canBeRemovedImmediately() {
    if (!getPendingRequests().isEmpty() || getCurrentRequest() != null) {
      return false;
    }
    return true;
  }

  /**
   * Determines if the mine building can accept a request.
   * If the building is marked for removal, it rejects all new requests.
   *
   * @param request the request to be considered
   * @return true if the request is acceptable, false otherwise
   */
  @Override
  public boolean canAcceptRequest(Request request) {
    return !isPendingRemoval();
  }
}
