package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Represents a delivery of a specific item from a source building to a
 * destination building.
 * Each delivery contains an item, its quantity, and a remaining delivery time.
 */
public class Delivery {
  final Building source;
  final Building destination;
  final Item item;
  final int quantity;
  int deliveryTime;
  int pathIndex;
  int stepIndex;
  Coordinate currentCoordinate;

  /**
   * Constructs a Delivery object with specified source, destination, item,
   * quantity, and delivery time.
   *
   * @param source       the source building from which the item originates
   * @param destination  the destination building to which the item is delivered
   * @param item         the item being delivered
   * @param quantity     the number of items being delivered
   * @param deliveryTime the number of cycles required for the delivery to
   *                     complete
   */
  public Delivery(Building source, Building destination, Item item, int quantity, int deliveryTime) {
    this.source = source;
    this.destination = destination;
    this.item = item;
    this.quantity = quantity;
    this.deliveryTime = deliveryTime;
    this.pathIndex = 0;
    this.stepIndex = 0;
    this.currentCoordinate = source.getLocation();
  }

  public Delivery(Building source, Building destination, Item item, int quantity, int deliveryTime, int pathIndex) {
    this.source = source;
    this.destination = destination;
    this.item = item;
    this.quantity = quantity;
    this.deliveryTime = deliveryTime;
    this.pathIndex = pathIndex;
    this.stepIndex = 0;
    this.currentCoordinate = source.getLocation();
  }

  public Delivery(Building source, Building destination, Item item, int quantity, int deliveryTime, int pathIndex,
      int stepIndex, Coordinate currentCoordinate) {
    this.source = source;
    this.destination = destination;
    this.item = item;
    this.quantity = quantity;
    this.deliveryTime = deliveryTime;
    this.pathIndex = pathIndex;
    this.stepIndex = stepIndex;
    this.currentCoordinate = currentCoordinate;
  }

  /**
   * Decreases the delivery time by one cycle, simulating one timestep in the
   * simulation.
   */
  public void step() {
    if (deliveryTime > 0) {
      deliveryTime--;
      stepIndex++;
    }
  }

  /**
   * Checks whether the delivery has arrived at its destination.
   *
   * @return true if deliveryTime is 0, indicating arrival; false otherwise
   */
  public boolean isArrive() {
    return deliveryTime == 0;
  }

  /**
   * Completes the delivery by adding the item to the destination's storage and
   * notifying the simulation.
   * Should only be called when the delivery has arrived.
   */
  public void finishDelivery() {
    destination.addToStorage(item, quantity);

    // if delivering to a waste disposal building, release reserved capacity
    if (destination instanceof WasteDisposalBuilding) {
      WasteDisposalBuilding wasteDisposal = (WasteDisposalBuilding) destination;
      wasteDisposal.releaseReservedCapacity(item, quantity);
    }

    source.getSimulation().onIngredientDelivered(item, destination, source);
  }

  public void updateCurrentCoordinate(List<Path> pathList) {
    for (Path path : pathList) {
        if (path.isMatch(source.getLocation(), destination.getLocation())) {
            currentCoordinate=path.getSteps().get(stepIndex);
        }
    }
  }

    /**
     * Checks if the delivery is using a specific path.
     *
     * @param path the path to check
     * @return true if the delivery is using the specified path; false otherwise
     */
    public boolean isUsingPath(Path path) {
        return path.isMatch(source.getLocation(), destination.getLocation());
    }

  public Coordinate getCurrentCoordinate() {
    return currentCoordinate;
  }

  /**
   * Converts the delivery details to a JSON object for serialization or display.
   *
   * @return a JsonObject representing the delivery with source, destination, item
   *         name, quantity, and delivery time
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();

    json.addProperty("source", source.getName());
    json.addProperty("destination", destination.getName());
    json.addProperty("item", item.getName());
    json.addProperty("quantity", quantity);
    json.addProperty("deliveryTime", deliveryTime);
    json.addProperty("pathIndex", pathIndex);
    json.addProperty("stepIndex", stepIndex);
    json.addProperty("x", currentCoordinate.getX());
    json.addProperty("y", currentCoordinate.getY());
    return json;
  }
}
