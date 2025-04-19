package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

/**
 * Represents a delivery carried out by a drone.
 * This class extends the base Delivery class with drone-specific functionality.
 */
public class DroneDelivery extends Delivery {
  private final DronePort dronePort;
  private final Drone drone;
  private DeliveryState state;
  
  /**
   * Enumeration of the states a drone delivery can be in.
   */
  public enum DeliveryState {
    TO_SOURCE, // drone is flying from drone port to the source building
    TO_DESTINATION, // drone is flying from source to destination with the item
    RETURNING // drone is flying back to the drone port
  }
  
  /**
   * Creates a new drone delivery.
   *
   * @param dronePort    the drone port managing this delivery
   * @param drone        the drone performing the delivery
   * @param source       the source building
   * @param destination  the destination building
   * @param item         the item being delivered
   * @param quantity     the quantity of the item
   */
  public DroneDelivery(DronePort dronePort, Drone drone, Building source, Building destination, Item item, int quantity) {
    // initialize with travel time to source first
    super(source, destination, item, quantity, calculateDeliveryTime(dronePort.getBuilding().getLocation(), source.getLocation()));
    this.dronePort = dronePort;
    this.drone = drone;
    this.state = DeliveryState.TO_SOURCE;
    drone.setInUse(true);
    this.currentCoordinate = dronePort.getBuilding().getLocation();
    
    // log drone delivery creation if verbosity is high enough
    Simulation sim = source.getSimulation();
    if (sim.getVerbosity() > 0) {
      sim.getLogger().log("[drone delivery started]: Drone from port at " + dronePort.getBuilding().getName() + 
                        " dispatched to deliver " + quantity + " " + item.getName() + 
                        " from " + source.getName() + " to " + destination.getName());
    }
  }
  
  /**
   * Calculates the delivery time between two coordinates.
   *
   * @param from the starting coordinate
   * @param to   the ending coordinate
   * @return the calculated delivery time
   */
  private static int calculateDeliveryTime(Coordinate from, Coordinate to) {
    int dx = Math.abs(from.getX() - to.getX());
    int dy = Math.abs(from.getY() - to.getY());
    int distance = dx + dy;    
    
    int deliveryTime = (int) Math.ceil((double) distance / Drone.getSpeed());
    return Math.max(1, deliveryTime);
  }
  
  /**
   * Updates the delivery state by one time step.
   */
  @Override
  public void step() {
    if (deliveryTime > 0) {
      deliveryTime--;
      
      // if we've reached the destination for the current state
      if (deliveryTime == 0) {
        Simulation sim = source.getSimulation();
        switch (state) {
          case TO_SOURCE:
            // arrived at source, now head to destination
            currentCoordinate = source.getLocation();
            source.takeFromStorage(item, quantity);
            deliveryTime = calculateDeliveryTime(source.getLocation(), destination.getLocation());
            state = DeliveryState.TO_DESTINATION;
            if (sim.getVerbosity() > 0) {
              sim.getLogger().log("[drone at source]: Drone arrived at " + source.getName() + 
                                " to pick up " + quantity + " " + item.getName());
            }
            break;
            
          case TO_DESTINATION:
            // arrived at destination, deliver item and head back to drone port
            destination.addToStorage(item, quantity);
            // if delivering to a waste disposal building, release reserved capacity
            if (destination instanceof WasteDisposalBuilding) {
              WasteDisposalBuilding wasteDisposal = (WasteDisposalBuilding) destination;
              wasteDisposal.releaseReservedCapacity(item, quantity);
            }
            // notify simulation of delivery
            sim.onIngredientDelivered(item, destination, source);
            if (sim.getVerbosity() > 0) {
              sim.getLogger().log("[drone delivery complete]: Drone delivered " + quantity + " " + 
                                item.getName() + " to " + destination.getName());
            }
            currentCoordinate = destination.getLocation();
            deliveryTime = calculateDeliveryTime(destination.getLocation(), dronePort.getBuilding().getLocation());
            state = DeliveryState.RETURNING;
            break;
            
          case RETURNING:
            // returned to drone port, delivery is complete
            currentCoordinate = dronePort.getBuilding().getLocation();
            dronePort.returnDrone(drone);
            drone.setInUse(false);
            if (sim.getVerbosity() > 0) {
              sim.getLogger().log("[drone returned]: Drone returned to port at " + 
                                dronePort.getBuilding().getName());
            }
            break;
        }
      }
    }
  }
  
  /**
   * Determines if the drone delivery has completed all of its steps.
   *
   * @return true if the delivery is complete
   */
  @Override
  public boolean isArrive() {
    return deliveryTime == 0 && state == DeliveryState.RETURNING;
  }
  
  /**
   * Do nothing since item delivery is handled in the step method.
   */
  @Override
  public void finishDelivery() {
  }
  
  /**
   * Do nothing for drone deliveries since drone deliveries don't use paths and their coordinates are updated in their step method.
   */
  @Override
  public void updateCurrentCoordinate(java.util.List<Path> pathList) {
  }
  
  /**
   * Gets the current state of the drone delivery.
   *
   * @return the current delivery state
   */
  public DeliveryState getState() {
    return state;
  }
  
  /**
   * Gets the drone port associated with this delivery.
   *
   * @return the drone port
   */
  public DronePort getDronePort() {
    return dronePort;
  }
  
  /**
   * Gets the drone used for this delivery.
   *
   * @return the drone
   */
  public Drone getDrone() {
    return drone;
  }
  
  /**
   * Converts the drone delivery to a JSON object.
   *
   * @return a JsonObject representing this drone delivery
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    json.addProperty("type", "DroneDelivery");
    json.addProperty("dronePort", dronePort.getBuilding().getName());
    json.addProperty("state", state.name());
    return json;
  }
}