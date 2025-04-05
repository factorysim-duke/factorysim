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
  private final Recipe recipe;
  private int currentStockNum; // current stock is immediately available in the same cycle

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
    this.recipe = simulation.getRecipeForItem(storageItem);
    this.currentStockNum = 0;
  }

  public Item getStorageItem() {
    return storageItem;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }

  public double getPriority() {
    return priority;
  }

  public int getOutstandingRequestNum() {
    return outstandingRequestNum;
  }

  public void setOutstandingRequestNum(int outstandingRequestNum) {
    this.outstandingRequestNum = outstandingRequestNum;
  }

  public int getArrivingItemNum() {
    return arrivingItemNum;
  }

  public int getCurrentStockNum() {
    return currentStockNum;
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

  /**
   * Adds item to storage when a refill request is completed. These items will not
   * be available until next cycle.
   * 
   * @param item     is the item to be added.
   * @param quantity is the quantity of item to be added.
   * @throws IllegalArgumentException if item is not the storage item of this
   *                                  building, or the stock number will pass the
   *                                  maximum capacity.
   */
  @Override
  public void addToStorage(Item item, int quantity) {
    if (!item.equals(storageItem)) {
      throw new IllegalArgumentException("The storage building " + getName() + " cannot store " + item.getName());
    }
    int futureStock = currentStockNum + arrivingItemNum + quantity;
    if (futureStock > maxCapacity) {
      throw new IllegalArgumentException(
          "The storage building " + getName() + " cannot receive " + quantity + " more " + item.getName());
    }

    arrivingItemNum += quantity;
    outstandingRequestNum = Math.max(0, outstandingRequestNum - quantity);
  }
}
