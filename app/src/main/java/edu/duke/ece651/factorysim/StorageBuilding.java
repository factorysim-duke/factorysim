package edu.duke.ece651.factorysim;

import java.util.List;

import com.google.gson.JsonArray;
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
    this.currentStockNum = 0;
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

  /**
   * Converts the current status of building into JSON.
   * 
   * @return the JSON representation of the building with current status.
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getName());
    json.addProperty("stores", storageItem.getName());
    json.addProperty("capacity", maxCapacity);
    json.addProperty("priority", priority);

    JsonArray sourcesArray = new JsonArray();
    for (Building source : this.getSources()) {
      sourcesArray.add(source.getName());
    }
    json.add("sources", sourcesArray);

    JsonObject storageJson = new JsonObject();
    if (currentStockNum > 0) {
      storageJson.addProperty(storageItem.getName(), currentStockNum);
    }
    json.add("storage", storageJson);

    if (this.getLocation() != null) {
      json.addProperty("x", this.getLocation().getX());
      json.addProperty("y", this.getLocation().getY());
    }
    return json;
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

  /**
   * Reduce current stock number when a request to take items is completed.
   * 
   * @param item     is the item to be taken.
   * @param quantity is the quantity of item to be taken.
   * @throws IllegalArgumentException if item is not the storage item of this
   *                                  building, or the current stock number is not
   *                                  enough.
   */
  @Override
  public void takeFromStorage(Item item, int quantity) {
    if (!item.equals(storageItem)) {
      throw new IllegalArgumentException("The storage building " + getName() + " cannot store " + item.getName());
    }
    if (currentStockNum < quantity) {
      throw new IllegalArgumentException(
          "The storage building " + getName() + " does not have enough " + item.getName());
    }

    currentStockNum -= quantity;
  }

  /**
   * Steps the building forward in time.
   */
  @Override
  public void step() {
    // try to complete pending request using currently available stocks
    // can give away many at a time, and use fifo only to choose request
    
    while (!getPendingRequest().isEmpty() && currentStockNum > 0) {
      Request request = getPendingRequests().remove(0); // use fifo only
      if (request.isUserRequest()) {
        takeFromStorage(storageItem, 1);
        getSimulation().onRequestCompleted(request);
      } else {
        Building destination = request.getDeliverTo();
        deliverTo(destination, storageItem, 1);
        takeFromStorage(storageItem, 1);
      }
    }

    // by the end of each time step, the arriving items becomes available
    currentStockNum += arrivingItemNum;
    arrivingItemNum = 0;

    // periodically make refill requests from sources
    int pendingRequestsCount = getPendingRequests().size();
    int R = maxCapacity - currentStockNum - outstandingRequestNum + pendingRequestsCount;
    
    if (R > 0) {
      int T = maxCapacity;
      int F = (int) Math.ceil((double) (T * T) / (R * priority));
      int currentTime = getSimulation().getCurrentTime();
      
      if (currentTime % F == 0) {
        List<Building> availableSources = getAvailableSourcesForItem(storageItem);
        
        if (!availableSources.isEmpty()) {
          Building selectedSource = sourcePolicy.selectSource(
              storageItem,
              availableSources,
              (building, score) -> {
              });
          if (selectedSource != null) {
            int orderNum = getSimulation().getOrderNum();
            Recipe recipe = getSimulation().getRecipeForItem(storageItem);
            Request newRequest = new Request(orderNum, storageItem, recipe, selectedSource, this);
            outstandingRequestNum++;
            
            selectedSource.addRequest(newRequest);
          }
        }
      }
    }
  }

  /**
   * Gets the current storage number of an item.
   * 
   * @param item is the item to be checked.
   * @return -1 if the requested item is not in storage, otherwise the current
   *         storage number of that item.
   */
  public int getStorageNumberOf(Item item) {
    if (!storageItem.equals(item)) {
      return -1;
    } else {
      return currentStockNum;
    }
  }

  /**
   * Gets the storage item.
   * 
   * @return the storage item.
   */
  public Item getStorageItem() {
    return storageItem;
  }

  /**
   * Gets the maximum capacity.
   * 
   * @return the maximum capacity.
   */
  public int getMaxCapacity() {
    return maxCapacity;
  }

  /**
   * Gets the priority number.
   * 
   * @return the priority number.
   */
  public double getPriority() {
    return priority;
  }

  /**
   * Gets the number for arriving items (which are not available until next
   * cycle).
   * 
   * @return the number of arricing items.
   */
  public int getArrivingItemNum() {
    return arrivingItemNum;
  }

  /**
   * Gets the number of current stock.
   * 
   * @return the number of current stock.
   */
  public int getCurrentStockNum() {
    return currentStockNum;
  }

  /**
   * Gets the queue length for this storage building.
   * 
   * @return the queue length (negative if items are in stock)
   */
  @Override
  public int getNumOfPendingRequests() {
    if (currentStockNum > 0) {
      // treat items in stock as negative queue entries
      return -currentStockNum;
    } else {
      // if no stock, behave like factory
      return super.getNumOfPendingRequests();
    }
  }

  /**
   * Gets the sum of remaining latencies for this storage building.
   * 
   * @return the sum of remaining latencies (negative if items are in stock)
   */
  @Override
  public int sumRemainingLatencies() {
    if (currentStockNum > 0) {
      // if we have items in stock, return negative latency
      // latency = -(recipe latency * number of items in stock)
      int recipeLatency = getSimulation().getRecipeForItem(storageItem).getLatency();
      return -(recipeLatency * currentStockNum);
    } else {
      // if no stock, behave like factory
      return super.sumRemainingLatencies();
    }
  }

  /**
   * Checks if this storage building can be removed immediately.
   * A storage building can be removed immediately if it has no requests in its
   * queue,
   * no items in storage, and no outstanding requests for more items to store.
   *
   * @return true if the building can be removed immediately, false otherwise.
   */
  @Override
  public boolean canBeRemovedImmediately() {
    if (!getPendingRequests().isEmpty() || getCurrentRequest() != null) {
      return false;
    }
    if (currentStockNum > 0 || outstandingRequestNum > 0 || arrivingItemNum > 0) {
      return false;
    }
    return true;
  }

  /**
   * Determines if the storage building can accept a request.
   * If the building is marked for removal, it only accepts requests that
   * would take items from storage to help empty it.
   *
   * @param request the request to be considered
   * @return true if the request is acceptable, false otherwise
   */
  @Override
  public boolean canAcceptRequest(Request request) {
    if (!isPendingRemoval()) {
      return true;
    }
    if (request.getDeliverTo() != this && request.getProducer() == this) {
      return true;
    }
    return false;
  }
}
