package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Represents a drone port building in the simulation.
 * This building can construct and manage drones for delivering items.
 */
public class DronePortBuilding extends Building {
  private final DronePort dronePort;

  /**
   * Constructs a drone port building.
   *
   * @param name       the name of this building
   * @param sources    the list of source buildings
   * @param simulation the simulation this building belongs to
   */
  public DronePortBuilding(String name, List<Building> sources, Simulation simulation) {
    super(name, sources, simulation);
    this.dronePort = new DronePort(this);
  }

  /**
   * Gets the drone port associated with this building.
   *
   * @return the drone port
   */
  public DronePort getDronePort() {
    return dronePort;
  }

  /**
   * Creates a new drone at this drone port.
   *
   * @return true if the drone was successfully created, false if the port is at maximum capacity
   */
  public boolean createDrone() {
    boolean created = dronePort.createDrone();
    if (created && getSimulation().getVerbosity() > 0) {
      getSimulation().getLogger().log("[drone created]: New drone created at port " + getName() + 
                                     ", total drones: " + dronePort.getDroneCount());
    }
    return created;
  }

  /**
   * Gets the number of drones currently at this port.
   *
   * @return the number of drones
   */
  public int getDroneCount() {
    return dronePort.getDroneCount();
  }

  /**
   * Gets the maximum number of drones this port can hold.
   *
   * @return the maximum number of drones
   */
  public int getMaxDrones() {
    return dronePort.getMaxDrones();
  }

  /**
   * Checks if this building can produce the specified item.
   * Drone ports do not directly produce any items.
   *
   * @param item the item to check
   * @return false, as drone ports don't produce items
   */
  @Override
  public boolean canProduce(Item item) {
    return false;
  }

  /**
   * Checks if the building can be removed immediately.
   * A drone port can be removed if it has no pending requests.
   *
   * @return true if the building can be removed immediately
   */
  @Override
  public boolean canBeRemovedImmediately() {
    return getNumOfPendingRequests() == 0 && !isProcessing();
  }

  /**
   * Converts this building to a JSON object.
   *
   * @return a JSON object representing this building
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", getName());
    json.addProperty("type", "DronePort");
    
    JsonObject dronePortInfo = dronePort.toJson();
    json.addProperty("droneCount", dronePort.getDroneCount());
    json.addProperty("maxDrones", dronePort.getMaxDrones());
    json.addProperty("radius", dronePort.getRadius());
    
    JsonArray sourcesArray = new JsonArray();
    for (Building source : getSources()) {
      sourcesArray.add(source.getName());
    }
    json.add("sources", sourcesArray);
    
    if (getLocation() != null) {
      json.addProperty("x", getLocation().getX());
      json.addProperty("y", getLocation().getY());
    }
    
    json.add("drones", dronePortInfo.get("drones"));
    
    return json;
  }
}