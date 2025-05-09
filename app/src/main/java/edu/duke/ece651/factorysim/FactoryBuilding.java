package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Represents a factory building in the simulation.
 */
public class FactoryBuilding extends Building {
  private final Type factoryType;
  private int remainingLatency;

  /**
   * Calculates the sum of latency of all recipes.
   * 
   * @return the sum of all recipe's latency.
   */
  private static int calculateDefaultLatency(Type factoryType) {
    int ans = 0;
    List<Recipe> recipes = factoryType.getRecipes();
    for (int i = 0; i < recipes.size(); i++) {
      ans += recipes.get(i).getLatency();
    }
    return ans;
  }

  /**
   * Constructs a basic factory with empty storage and latency being the sum of
   * those in all recipes.
   *
   * @param factoryType is the type of factory.
   * @param name        is the name of the building.
   * @param sources     is the list of buildings where this factory can get
   *                    ingredients from.
   * @param simulation  is the injected simulation instance.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public FactoryBuilding(Type factoryType, String name, List<Building> sources, Simulation simulation) {
    super(name, sources, simulation);
    this.factoryType = factoryType;
    this.remainingLatency = calculateDefaultLatency(factoryType);
  }

  /**
   * Gets the factory type of this factory.
   * 
   * @return the factory type of this factory.
   */
  public Type getFactoryType() {
    return factoryType;
  }

  /**
   * Checks if this factory can produce a given item.
   * 
   * @param item is the item to be checked.
   * @return true if this factory can produce this item, false otherwise.
   */
  public boolean canProduce(Item item) {
    List<Recipe> recipes = factoryType.getRecipes();
    for (int i = 0; i < recipes.size(); i++) {
      if (recipes.get(i).getOutput().getName().equals(item.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the remaining latency.
   * 
   * @return the remaining latency.
   */
  public int getRemainingLatency() {
    return remainingLatency;
  }

  /**
   * Converts the factory building to a JSON object.
   *
   * @return a JsonObject representing the factory building.
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getName());
    json.addProperty("type", factoryType.getName());
    JsonArray sourcesArray = new JsonArray();
    for (Building source : this.getSources()) {
      sourcesArray.add(source.getName());
    }
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

  /**
   * Checks if this factory building can be removed immediately.
   * A factory building can be removed immediately if it has no requests in its
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
   * Determines if the factory building can accept a request.
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
