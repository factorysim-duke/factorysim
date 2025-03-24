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
   * @param miningRecipe  is the recipe for this mine.
   * @param name          is the name of the building.
   * @param simulation    is the injected simulation instance.
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

  public JsonObject toJson(){
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getName());
    json.addProperty("mine", miningRecipe.getOutput().getName());
    JsonArray sourcesArray=new JsonArray();
    json.add("sources", sourcesArray);
    JsonObject storage = new JsonObject();
    if(!getStorage().isEmpty()){
      for (Map.Entry<Item,Integer>entry:getStorage().entrySet()){
        storage.addProperty(entry.getKey().getName(), entry.getValue());
      }
    }
    json.add("storage", storage);
//    json.addProperty("remainingLatency", remainingLatency);
    return json;
  }
}
