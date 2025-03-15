package edu.duke.ece651.factorysim;

import java.util.ArrayList;

/**
 * Represents a mine building in the simulation.
 */
public class MineBuilding extends Building {
  private final Recipe miningRecipe;
  private final Item resource; // got from miningRecipe
  private final int miningLatency; // got from miningRecipe

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
    this.miningLatency = miningRecipe.getLatency();
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
    return miningLatency;
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
}
