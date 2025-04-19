package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

/**
 * Represents a drone in the simulation.
 * Drones are used by drone ports to deliver items between buildings.
 */
public class Drone {
  private boolean inUse = false;
  private static final int SPEED = 5;
  
  /**
   * Creates a new drone.
   */
  public Drone() {
  }
  
  /**
   * Gets the speed of the drone in tiles per timestep.
   * 
   * @return the speed of the drone
   */
  public static int getSpeed() {
    return SPEED;
  }
  
  /**
   * Checks if the drone is currently in use.
   * 
   * @return true if the drone is in use, false otherwise
   */
  public boolean isInUse() {
    return inUse;
  }
  
  /**
   * Sets the drone's usage status.
   * 
   * @param inUse true if the drone is being used, false otherwise
   */
  public void setInUse(boolean inUse) {
    this.inUse = inUse;
  }
  
  /**
   * Converts the drone to a JSON object.
   * 
   * @return a JSON object representing this drone
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("inUse", inUse);
    return json;
  }
}