package edu.duke.ece651.factorysim;

import java.util.List;

import com.google.gson.JsonObject;

/**
 * Represents a storage building in the simulation.
 */
public class StorageBuilding extends Building {
  private final Item storageItem;
  private final int maxCapacity;
  private final double priority;
  private int outstandingRequestNum;
  private int arrivingItemNum; // number of items arriving this cycle, which will become available next cycle

  /**
   * Constructs a storage building.
   *
   * @param name        is the name of the building.
   * @param sources     is the list of buildings where this factory can get
   *                    ingredients from.
   * @param simulation  is the injected simulation instance.
   * @param storageItem is the item in storage for this building.
   * @param maxCapacity is the maximum capacity number.
   * @param priority    is a settings value read from JSON file to decide how
   *                    aggressively should make requests for refills.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public StorageBuilding(String name, List<Building> sources, Simulation simulation, Item storageItem, int maxCapacity,
      double priority) {
    super(name, sources, simulation);
    this.storageItem = storageItem;
    this.maxCapacity = maxCapacity;
    this.priority = priority;
    this.outstandingRequestNum = 0;
    this.arrivingItemNum = 0;
  }
  /**
   * Checks if this storage building can give an item.
   * 
   * @param item is the item to be checked.
   * @return true if this storage building can give this item, false otherwise.
   */
  @Override
  public boolean canProduce(Item item) {
    return storageItem.equals(item);
  }

  @Override
  public JsonObject toJson() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toJson'");
  }

}
