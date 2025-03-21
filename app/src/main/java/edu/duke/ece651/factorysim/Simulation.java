package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Runs the factory simulation, managing buildings and item production.
 */
public class Simulation {
  private final World world;
  private final Map<String, RequestPolicy> requestPolicies = new HashMap<>();
  private final Map<String, SourcePolicy> sourcePolicies = new HashMap<>();
  private RequestPolicy defaultRequestPolicy = new FifoRequestPolicy();
  private SourcePolicy defaultSourcePolicy = new QLenSourcePolicy();

  private int currentTime;
  private boolean finished = false;
  private int nextOrderNum = 0;

  private int verbosity;

  private Logger logger;

  /**
   * Creates a simulation from a JSON configuration file.
   *
   * @param jsonFilePath the path to the JSON file.
   * @param verbosity the initial verbosity.
   * @param logger the injected logger.
   */
  public Simulation(String jsonFilePath, int verbosity, Logger logger) {
    this.currentTime = 0;

    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    this.world = WorldBuilder.buildWorld(configData, this);

    this.verbosity = verbosity;

    this.logger = logger;
  }

  /**
   * Creates a simulation from a JSON configuration file with initial verbosity 0.
   * The default logger is a `StreamLogger` logging into stdout (`System.out`).
   *
   * @param jsonFilePath the path to the JSON file.
   */
  public Simulation(String jsonFilePath) {
    this(jsonFilePath, 0, new StreamLogger(System.out));
  }

  /**
   * Sets the policy for the given type and target.
   * 
   * @param policy the policy to set
   * @param target the target to set the policy for
   */
  public void setPolicy(String type, String policy, String target) {
    if (!type.equals("request") && !type.equals("source")) {
      throw new IllegalArgumentException("Policy type must be request or source, but is " + type);
    }

    if (policy.equals("default")) {
      resetPolicy(target);
      return;
    }

    Policy policyInstance = null;
    // Get or create the policy instance
    if (type.equals("request")) {
      policyInstance = RequestPolicyFactory.createPolicy(policy);
    } else {
      policyInstance = SourcePolicyFactory.createPolicy(policy);
    }
    switch (target) {
      case "*":
        applyPolicyToAllBuildings(policyInstance);
        break;
      case "default":
        if (type.equals("request")) {
          RequestPolicy requestPolicy = (RequestPolicy) policyInstance;
          defaultRequestPolicy = requestPolicy;
        } else {
          SourcePolicy sourcePolicy = (SourcePolicy) policyInstance;
          defaultSourcePolicy = sourcePolicy;
        }
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
      sourcePolicies.clear();
    } else if (target.equals("default")) {
      throw new IllegalArgumentException("Cannot set 'default' policy on 'default'");
    } else {
      if (!world.hasBuilding(target)) {
        throw new IllegalArgumentException("Building '" + target + "' does not exist.");
      }
      // Remove custom policy, revert to default
      requestPolicies.remove(target);
      sourcePolicies.remove(target);
    }
  }

  /**
   * Applies a request policy to all buildings.
   * 
   * @param policyInstance the policy to apply.
   */
  private void applyPolicyToAllBuildings(Policy policyInstance) {
    for (Building building : world.getBuildings()) {
      if (policyInstance.getPolicyTypeName() == "request") {
        RequestPolicy requestPolicy = (RequestPolicy) policyInstance;
        requestPolicies.put(building.getName(), requestPolicy);
      } else {
        SourcePolicy sourcePolicy = (SourcePolicy) policyInstance;
        sourcePolicies.put(building.getName(), sourcePolicy);
      }
    }
  }

  /**
   * Applies a request policy to a specific building after checking its existence.
   * 
   * @param policyInstance the policy to apply.
   * @param buildingName   the name of the building to apply the policy to.
   */
  private void applyPolicyToBuilding(Policy policyInstance, String buildingName) {
    if (!world.hasBuilding(buildingName)) {
      throw new IllegalArgumentException("Building '" + buildingName + "' does not exist.");
    }
    if (policyInstance.getPolicyTypeName() == "request") {
      RequestPolicy requestPolicy = (RequestPolicy) policyInstance;
      requestPolicies.put(buildingName, requestPolicy);
    } else {
      SourcePolicy sourcePolicy = (SourcePolicy) policyInstance;
      sourcePolicies.put(buildingName, sourcePolicy);
    }
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
   * Gets the source policy for the given building.
   * If the building is not in the source policies, the default source policy is
   * returned.
   * 
   * @param building the building to get the policy for.
   * @return the source policy for the given building.
   */
  public SourcePolicy getSourcePolicy(String building) {
    return sourcePolicies.getOrDefault(building, defaultSourcePolicy);
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
    Request userRequest = new Request(getOrderNum(), item, recipe, producer, null);
    producer.addRequest(userRequest);
  }

  /**
   * Runs the simulation until all requests are completed, then exits.
   */
  public void finish() {
    while (!allRequestsFinished()) {
      step(1);
    }
    logger.log("Simulation completed at time-step " + currentTime);
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

  /**
   * Gets the recipe for a given item in the simulation.
   * 
   * @param item is the requested item.
   * @return the recipe for the item.
   */
  public Recipe getRecipeForItem(Item item) {
    return world.getRecipeForItem(item);
  }

  /**
   * Returns the order number, then increases the order number by 1.
   * 
   * @return the order number.
   */
  public int getOrderNum() {
    return nextOrderNum++;
  }

  /**
   * Set the verbosity of the simulation.
   *
   * @param n new verbosity.
   */
  public void setVerbosity(int n) {
    this.verbosity = n;
  }

  /**
   * Get the current verbosity level.
   *
   * @return current verbosity level.
   */
  public int getVerbosity() {
    return verbosity;
  }

  public Logger getLogger() {
    return this.logger;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  /**
   * Indicates a request was completed.
   * If verbosity >= 0, log order completion details.
   *
   * @param completed the completed request instance.
   */
  public void onRequestCompleted(Request completed) {
    if (verbosity < 0) {
      return;
    }

    String m = "[order complete] Order " + completed.getOrderNum() +
               " completed (" + completed.getItem().getName() +
               ") at time " + currentTime;
    logger.log(m);
  }

  /**
   * Indicates an ingredient has been assigned to a source.
   * If verbosity >= 1, log ingredient assignment details.
   *
   * @param item is the ingredient item assigned.
   * @param assigned is the assigned building for producing the ingredient (in other words, the producer).
   * @param deliverTo is the building to deliver the ingredient to.
   */
  public void onIngredientAssigned(Item item, Building assigned, Building deliverTo) {
    if (verbosity < 1) {
      return;
    }

    String m = "[ingredient assignment]: " + item.getName() +
               " assigned to " + assigned.getName() +
               " to deliver to " + deliverTo.getName();
    logger.log(m);
  }

  /**
   * Indicates an ingredient has been delivered.
   * If verbosity >= 1, log ingredient delivery details.
   *
   * @param item is the ingredient item delivered.
   * @param to is the receiver of the delivery.
   * @param from is the deliverer of the delivery.
   */
  public void onIngredientDelivered(Item item, Building to, Building from) {
    if (verbosity < 1) {
      return;
    }

    // Log ingredient delivery info
    String m = "[ingredient delivered]: " + item.getName() +
               " to " + to.getName() +
               " from " + from.getName() +
               " on cycle " + getCurrentTime();
    logger.log(m);

    // Log ready ingredients (only when `to` is a factory since only factory have recipes)
    if (to instanceof FactoryBuilding factory) {
      List<Recipe> recipes = factory.getFactoryType().getRecipes();
      for (int i = 0; i < recipes.size(); i++) {
        logger.log("    " + i + ": " + item.getName() + " is ready");
      }
    }
  }
}
