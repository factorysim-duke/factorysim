package edu.duke.ece651.factorysim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a building in the simulation.
 */
public abstract class Building {
  private final String name;
  private final List<Building> sources;
  private HashMap<Item, Integer> storage;
  private Queue<Request> requestQueue;
  private boolean isProcessing = false;

  /**
   * Constructs a basic building with empty storage.
   * 
   * @param name    is the name of the building.
   * @param sources is the list of buildings where this building can get
   *                ingredients from.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public Building(String name, List<Building> sources) {
    if (Utils.isNameValid(name) == false) {
      throw new IllegalArgumentException(
          "Building name cannot contain " + Utils.notAllowedInName + ", but is: " + name);
    }
    this.name = name;
    this.sources = sources;
    this.storage = new HashMap<>();
    this.requestQueue = new LinkedList<>();
  }

  /**
   * Gets the name of the building.
   * 
   * @return name of the building.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the sources of the building.
   * 
   * @return list of sources of the building.
   */
  public List<Building> getSources() {
    return sources;
  }

  /**
   * Updates the sources of the building.
   * 
   * @param newSources is the new list of sources.
   */
  public void updateSources(List<Building> newSources) {
    sources.clear();
    sources.addAll(newSources);
  }

  /**
   * Gets the current storage number of an item.
   * 
   * @param item is the item to be checked.
   * @return -1 if the requested item is not in storage, otherwise the current
   *         storage number of that item.
   */
  public int getStorageNumberOf(Item item) {
    if (storage.containsKey(item) == false) {
      return -1;
    } else {
      return storage.get(item);
    }
  }

  /**
   * Update the storage by adding things in.
   * 
   * @param item     is the item to be updated.
   * @param quantity is the number of the item to be added into storage.
   */
  public void addToStorage(Item item, int quantity) {
    int existingNum = 0;
    if (storage.containsKey(item)) {
      existingNum = storage.get(item);
    }
    storage.put(item, existingNum + quantity);
  }

  /**
   * Update the storage by taking things out.
   * 
   * @param item     is the item to be updated.
   * @param quantity is the number of the item to be taken out of storage.
   * @throws IllegalArgumentException if the item does not exist in storage, or
   *                                  there isn't enough number to be taken out.
   */
  public void takeFromStorage(Item item, int quantity) {
    if (storage.containsKey(item) == false) {
      throw new IllegalArgumentException(
          "Cannot take " + item.getName() + " out of " + name + "'s storage, because it's not in stock.");
    }
    int storageNum = storage.get(item);
    if (storageNum < quantity) {
      throw new IllegalArgumentException("Cannot take " + quantity + " " + item.getName() + " out of " + name
          + "'s storage, because there isn't enough stock.");
    }
    int updatedStorageNum = storageNum - quantity;
    // if there is no storage left, delete the item from storage
    if (updatedStorageNum == 0) {
      storage.remove(item);
    } else {
      storage.put(item, updatedStorageNum);
    }
  }

  /**
   * Delivers things to another building.
   * 
   * @param destination is the destination building.
   * @param item        is the item to be delivered.
   * @param quantity    is the quantity of item to be delivered.
   */
  public void deliverTo(Building destination, Item item, int quantity) {
    destination.addToStorage(item, quantity);
  }

  /**
   * Add a new request to the request queue.
   *
   * @param request The request to be added.
   */
  public void addRequest(Request request) {
    requestQueue.offer(request);
  }

  /**
   * Checks if the factory/building has finished processing all requests.
   *
   * @return true if there are no active requests and nothing is being processed, false otherwise.
   */
  public boolean isFinished(){
    return !isProcessing && requestQueue.isEmpty();
  }

    /**
     * Steps the building forward in time.
     */
  public void step() {
    // do nothing by default
  requestQueue.poll();
  }

    /**
     * Checks if this building can produce a given item.
     *
     * @param item is the item to be checked.
     * @return true if this building can produce this item, false otherwise.
     */
  public abstract boolean canProduce(Item item);


}
