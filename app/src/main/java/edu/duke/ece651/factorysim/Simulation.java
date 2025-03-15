
package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Runs the factory simulation, managing buildings and item production.
 */
public class Simulation {
  private final World world;
  private final Map<String, RequestPolicy> requestPolicies = new HashMap<>();
  // private final Map<String, String> sourcePolicies = new HashMap<>();
  private RequestPolicy defaultRequestPolicy = new FifoRequestPolicy();
  // private SourcePolicy defaultSourcePolicy = new QLenSourcePolicy();

  private int currentTime;
  private boolean finished = false;
  private int nextOrderNum = 0;

  /**
   * Creates a simulation from a JSON configuration file.
   *
   * @param jsonFilePath the path to the JSON file.
   */
  public Simulation(String jsonFilePath) {
    this.currentTime = 0;
    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    this.world = WorldBuilder.buildWorld(configData, this);
  }

  /**
   * Sets the policy for the given type and target.
   * 
   * @param type   the type of policy to set
   * @param policy the policy to set
   * @param target the target to set the policy for
   */
  public void setPolicy(String type, String policy, String target) {
    if (!type.equals("request")) {
      throw new UnsupportedOperationException("Source policy is not implemented yet.");
    }

    if (policy.equals("default")) {
      resetPolicy(target);
      return;
    }

    // Get or create the policy instance
    RequestPolicy policyInstance = RequestPolicyFactory.createPolicy(policy);

    switch (target) {
      case "*":
        applyPolicyToAllBuildings(policyInstance);
        break;
      case "default":
        defaultRequestPolicy = policyInstance;
        break;
      default:
        applyPolicyToBuilding(policyInstance, target);
        break;
    }
  }

  /**
   * Resets the policy for the given target.
   * 
   * @param target the target to reset the policy for
   */
  private void resetPolicy(String target) {
    if (target.equals("*")) {
      // Reset all policies
      requestPolicies.clear();
    } else if (target.equals("default")) {
      throw new IllegalArgumentException("Cannot set 'default' policy on 'default'");
    } else {
      if (!world.hasBuilding(target)) {
        throw new IllegalArgumentException("Building '" + target + "' does not exist.");
      }
      // Remove custom policy, revert to default
      requestPolicies.remove(target);
    }
  }

  /**
   * Applies a policy to all buildings.
   * 
   * @param policyInstance the policy to apply.
   */
  private void applyPolicyToAllBuildings(RequestPolicy policyInstance) {
    for (Building building : world.getBuildings()) {
      requestPolicies.put(building.getName(), policyInstance);
    }
  }

  /**
   * Applies a policy to a specific building after checking its existence.
   */
  private void applyPolicyToBuilding(RequestPolicy policyInstance, String buildingName) {
    if (!world.hasBuilding(buildingName)) {
      throw new IllegalArgumentException("Building '" + buildingName + "' does not exist.");
    }
    requestPolicies.put(buildingName, policyInstance);
  }

  /**
   * Gets the request policy for the given building.
   * If the building is not in the request policies, the default request policy is
   * returned.
   * 
   * @param building the building to get the policy for.
   * @return the request policy for the given building.
   */
  public RequestPolicy getRequestPolicy(String building) {
    return requestPolicies.getOrDefault(building, defaultRequestPolicy);
  }

  /**
   * Working on the simulation for n steps.
   *
   * @param n the number of steps.
   * @throws IllegalArgumentException if n is less than 1 or >= Integer.MAX_VALUE.
   */
  public void step(int n) {
    if (n < 1 || n == Integer.MAX_VALUE) {
      throw new IllegalArgumentException("The number of step must be positive and not too large.");
    }
    for (int i = 0; i < n; i++) {
      currentTime++;
      for (Building building : world.getBuildings()) {
        building.step();
      }
    }
  }

  /**
   * Requests a building to produce an item with no delivery target (because it's
   * a user request).
   *
   * @param itemName     the name of the item to produce.
   * @param buildingName the name of the building to produce the item.
   * @throws IllegalArgumentException if the recipe for the item does not exist,
   *                                  no building can produce the item, or the
   *                                  building is not found.
   */
  public void makeUserRequest(String itemName, String buildingName) {
    Building producer = null;
    for (Building building : world.getBuildings()) {
      if (building.getName().equals(buildingName)) {
        producer = building;
        break;
      }
    }
    Item item = new Item(itemName);
    // check if the recipe exists
    Recipe recipe = world.getRecipeForItem(item);
    if (recipe == null) {
      throw new IllegalArgumentException("The recipe for item " + itemName + " does not exist.");
    }
    // check if the producer building exists
    if (producer == null) {
      throw new IllegalArgumentException("The building " + buildingName + " does not exist.");
    }
    // check if the building can produce the item
    if (producer.canProduce(item) == false) {
      throw new IllegalArgumentException("The building " + buildingName + " cannot produce item " + itemName);
    }
    // if all is valid, add the request
    nextOrderNum += 1;
    Request userRequest = new Request(nextOrderNum, item, recipe, producer, null);
    producer.addRequest(userRequest);
  }

  /**
   * Runs the simulation until all requests are completed, then exits.
   */
  public void finish() {
    while (!allRequestsFinished()) {
      step(1);
    }
    System.out.println("Simulation completed at time-step " + currentTime);
    finished = true;
  }

  /**
   * Checks if all building completed all their request.
   *
   * @return true if all buildings are done, false otherwise.
   */
  public boolean allRequestsFinished() {
    for (Building building : world.getBuildings()) {
      if (!building.isFinished()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the simulation is finished.
   *
   * @return true if finished, false otherwise.
   */
  public boolean isFinished() {
    return finished;
  }

  /**
   * Gets the current time step.
   *
   * @return the current time.
   */
  public int getCurrentTime() {
    return currentTime;
  }
}
