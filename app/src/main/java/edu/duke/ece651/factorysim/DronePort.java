package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a drone port in the simulation.
 * This class manages drones and handles drone delivery operations.
 */
public class DronePort {
  private final Building building;
  private int radius = 20;
  private List<Drone> drones;
  private int maxDrones = 10;

  public List<Drone> getDrones() { return this.drones; }
  public void setDrones(List<Drone> drones) { this.drones = drones; }

  /**
   * Constructs a drone port.
   *
   * @param building the building this drone port is attached to
   */
  public DronePort(Building building) {
    this.building = building;
    this.drones = new ArrayList<>();
  }

  /**
   * Gets the building this drone port is attached to.
   *
   * @return the building this drone port is attached to
   */
  public Building getBuilding() {
    return building;
  }

  /**
   * Creates a new drone and adds it to the drone port.
   *
   * @return true if the drone was successfully created, false if at maximum capacity
   */
  public boolean createDrone() {
    if (drones.size() < maxDrones) {
      drones.add(new Drone());
      return true;
    }
    return false;
  }

  /**
   * Gets the number of drones currently at this port.
   *
   * @return the number of drones
   */
  public int getDroneCount() {
    return drones.size();
  }

  /**
   * Gets the maximum number of drones this port can hold.
   *
   * @return the maximum number of drones
 ,  */
  public int getMaxDrones() {
    return maxDrones;
  }

  /**
   * Gets the radius of operation for this drone port.
   *
   * @return the radius in tiles
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Checks if a building is within the drone port's radius.
   *
   * @param targetBuilding the building to check
   * @return true if the building is within radius
   */
  public boolean isWithinRadius(Building targetBuilding) {
    Coordinate portLocation = building.getLocation();
    Coordinate buildingLocation = targetBuilding.getLocation();
    
    if (portLocation == null || buildingLocation == null) {
      return false;
    }
    
    int dx = Math.abs(portLocation.getX() - buildingLocation.getX());
    int dy = Math.abs(portLocation.getY() - buildingLocation.getY());
    
    return (dx + dy) <= radius;
  }

  /**
   * Checks if this drone port has an available drone.
   * 
   * @return true if an available drone exists
   */
  public boolean hasAvailableDrone() {
    return !drones.isEmpty();
  }

  /**
   * Gets an available drone for delivery.
   * 
   * @return a drone if available, null otherwise
   */
  public Drone getAvailableDrone() {
    if (drones.isEmpty()) {
      return null;
    }
    Drone drone = drones.remove(0);
    return drone;
  }

  /**
   * Returns a drone to the port after delivery.
   * 
   * @param drone the drone to return
   * @return true if the drone was returned, false if at maximum capacity
   */
  public boolean returnDrone(Drone drone) {
    if (drones.size() < maxDrones) {
      drones.add(drone);
      return true;
    }
    return false;
  }

  /**
   * Converts the drone port to a JSON object.
   * 
   * @return a JSON object representing this drone port
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("building", building.getName());
    json.addProperty("maxDrones", maxDrones);
    json.addProperty("radius", radius);
    
    JsonArray dronesArray = new JsonArray();
    for (Drone drone : drones) {
      dronesArray.add(drone.toJson());
    }
    json.add("drones", dronesArray);
    
    return json;
  }
}
