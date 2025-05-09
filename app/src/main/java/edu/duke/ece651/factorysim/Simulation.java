package edu.duke.ece651.factorysim;

import com.google.gson.*;
import edu.duke.ece651.factorysim.db.SessionDAO;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Runs the factory simulation, managing buildings and item production.
 */
public class Simulation {
  private World world;
  private final Map<String, RequestPolicy> requestPolicies = new HashMap<>();
  private final Map<String, SourcePolicy> sourcePolicies = new HashMap<>();
  private RequestPolicy defaultRequestPolicy = new FifoRequestPolicy();
  private SourcePolicy defaultSourcePolicy = new QLenSourcePolicy();

  private int currentTime;
  private boolean finished = false;
  private int nextOrderNum = 0;
  private int boardWidth = 1000;
  private int boardHeight = 100;

  private int verbosity;

  private Logger logger;
  private final List<Path> pathList = new ArrayList<>();
  DeliverySchedule deliverySchedule = new DeliverySchedule();

  // Event
  private final EventHandler<Building> onBuildingRemoved = new EventHandler<>();

  /**
   * Subscribe to the event when a building is removed.
   *
   * @param listener is the listener to subscribe.
   */
  public void subscribeToOnBuildingRemoved(Consumer<Building> listener) {
    onBuildingRemoved.subscribe(listener);
  }

  /**
   * Unsubscribes to the event when a building is removed.
   *
   * @param listener is the listener to unsubscribe.s
   */
  public void unsubscribeToOnBuildingRemoved(Consumer<Building> listener) {
    onBuildingRemoved.unsubscribe(listener);
  }

  /**
   * Creates a simulation from a JSON configuration file.
   *
   * @param jsonFilePath the path to the JSON file.
   * @param verbosity    the initial verbosity.
   * @param logger       the injected logger.
   */
  public Simulation(String jsonFilePath, int verbosity, Logger logger) {
    this.currentTime = 0;
    // this.finished = false;
    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    this.world = WorldBuilder.buildWorld(configData, this);
    // this.nextOrderNum = 0;
    this.verbosity = verbosity;
    this.logger = logger;

    // establish connections after world is fully initialized
    if (configData.connections != null && !configData.connections.isEmpty()) {
      establishConnections(configData.connections);
    }
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
   * Creates a simulation with an injected world.
   *
   * @param world     a `World` instance that's already constructed.
   * @param verbosity the initial verbosity.
   * @param logger    the injected logger.
   */
  public Simulation(World world, int verbosity, Logger logger) {
    this.currentTime = 0;
    this.world = world;
    this.verbosity = verbosity;
    this.logger = logger;
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
        world.getBuildingFromName(building.getName()).setRequestPolicy(requestPolicy);
      } else {
        SourcePolicy sourcePolicy = (SourcePolicy) policyInstance;
        sourcePolicies.put(building.getName(), sourcePolicy);
        world.getBuildingFromName(building.getName()).setSourcePolicy(sourcePolicy);
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
      world.getBuildingFromName(buildingName).setRequestPolicy(requestPolicy);
    } else {
      SourcePolicy sourcePolicy = (SourcePolicy) policyInstance;
      sourcePolicies.put(buildingName, sourcePolicy);
      world.getBuildingFromName(buildingName).setSourcePolicy(sourcePolicy);
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
      deliverySchedule.step(pathList);
      for (Building building : world.getBuildings()) {
        building.step();
      }
      currentTime++;
    }
    // check if any pending removals can be completed
    checkPendingRemovals();
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
    return this.world.getRecipeForItem(item);
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

  /**
   * Gets the logger.
   *
   * @return the logger.
   */
  public Logger getLogger() {
    return this.logger;
  }

  /**
   * Sets the logger.
   *
   * @param logger is the new content of the logger.
   */
  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  /**
   * Gets the `DeliverySchedule` instance of this simulation.
   *
   * @return the `DeliverySchedule` instance of this simulation.
   */
  public DeliverySchedule getDeliverySchedule() {
    return this.deliverySchedule;
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
   * @param item      is the ingredient item assigned.
   * @param assigned  is the assigned building for producing the ingredient (in
   *                  other words, the producer).
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
   * @param to   is the receiver of the delivery.
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

    // Log ready ingredients (only when `to` is a factory since only factory have
    // recipes)
    if (!to.getClass().equals(FactoryBuilding.class)) {
      return;
    }
    FactoryBuilding factory = (FactoryBuilding) to;
    List<Recipe> recipes = factory.getFactoryType().getRecipes();
    int i = 0;
    for (Recipe recipe : recipes) {
      if (factory.findMissingIngredients(recipe).isEmpty()) {
        logger.log("    " + i++ + ": " + recipe.getOutput().getName() + " is ready");
      }
    }
  }

  /**
   * Indicates a recipe is selected when a request is selected to be processed
   * next.
   * If verbosity >= 2, logs recipe selection details and all request details.
   *
   * @param building        is the factory building that selected the recipe.
   * @param requestPolicy   is the request policy used to select.
   * @param requests        is the list of pending requests (before removing the
   *                        selected request).
   * @param selectedRequest is the selected request instance.
   */
  public void onRecipeSelected(Building building,
      RequestPolicy requestPolicy,
      List<Request> requests,
      Request selectedRequest) {
    if (verbosity < 2) {
      return;
    }
    if (building.getClass().equals(MineBuilding.class)) {
      return; // Ignore calls from mines
    }
    // FactoryBuilding factory = (FactoryBuilding) building;

    // Log recipe selection
    logger.log("[recipe selection]: " + building.getName() +
        " has " + requestPolicy.getName() +
        " on cycle " + currentTime);

    // Log each request's information
    int selectedIndex = 0;
    for (int i = 0; i < requests.size(); i++) {
      // Get selected index
      if (requests.get(i) == selectedRequest) {
        selectedIndex = i;
      }

      // Log request information
      StringBuilder s = new StringBuilder("    " + i + ": ");
      List<Tuple<Item, Integer>> missingIngredients = building.findMissingIngredients(selectedRequest.getRecipe());
      if (missingIngredients.isEmpty()) {
        s.append("ready");
      } else {
        s.append("not ready, waiting on {");
        for (int j = 0; j < missingIngredients.size(); j++) {
          Tuple<Item, Integer> ingredient = missingIngredients.get(j);

          Item item = ingredient.first();
          int count = ingredient.second();

          if (count > 1) {
            s.append(count).append("x ");
          }
          s.append(item.getName());

          if (j != missingIngredients.size() - 1) {
            s.append(", ");
          }
        }
        s.append("}");
      }
      logger.log(s.toString());
    }

    // Log selected request
    logger.log("    Selecting " + selectedIndex);
  }

  /**
   * Indicates a source has been selected.
   * If verbosity >= 2, logs source selection details.
   *
   * @param building     is the factory building that selected the source.
   * @param sourcePolicy is the source policy used to select.
   * @param item         is the item that's requested so sourcing happens.
   */
  public void onSourceSelected(Building building,
      SourcePolicy sourcePolicy,
      Item item) {
    if (verbosity < 2) {
      return;
    }
    if (building.getClass().equals(MineBuilding.class)) {
      return; // Ignore calls from mines
    }
    // Log source selection
    logger.log("[source selection]: " + building.getName() +
        " (" + sourcePolicy.getName() +
        ") has request for " + item.getName() +
        " on " + currentTime);
  }

  /**
   * Indicates the source for an ingredient from a recipe has been selected.
   * If verbosity >= 2, logs source selection details.
   *
   * @param building       is the factory building that selected the source.
   * @param item           is the item being produced.
   * @param index          is the index of the ingredient.
   * @param ingredient     is the ingredient item.
   * @param sources        is a list of source buildings with their scores
   *                       calculated by the source policy used.
   * @param selectedSource is the selected source building.
   */
  public void onIngredientSourceSelected(Building building,
      Item item,
      int index,
      Item ingredient,
      List<Tuple<Building, Integer>> sources,
      Building selectedSource) {
    if (verbosity < 2) {
      return;
    }
    if (building.getClass().equals(MineBuilding.class)) {
      return; // Ignore calls from mines
    }
    // Log selection detail
    logger.log("[" + building.getName() + ":" + item.getName() + ":" + index +
        "] For ingredient " + ingredient.getName());

    // Log sources with scores
    for (Tuple<Building, Integer> source : sources) {
      Building sourceBuilding = source.first();
      int score = source.second();
      logger.log("    " + sourceBuilding.getName() + ": " + score);
    }

    // Log selected score
    logger.log("    Selecting " + selectedSource.getName());
  }

  /**
   * Indicates when waste is delivered to a waste disposal building.
   *
   * @param wasteType        is the item of waste delivered.
   * @param quantity         is the quantity of waste delivered.
   * @param disposalBuilding is the building receiving the waste.
   * @param sourceBuilding   is the building that produced the waste.
   */
  public void onWasteDelivered(Item wasteType, int quantity, Building disposalBuilding, Building sourceBuilding) {
    if (verbosity >= 1) {
      logger.log("[waste delivered]: " + quantity + " " + wasteType.getName() + " to " + disposalBuilding.getName()
          + " from " + sourceBuilding.getName() + " on cycle " + getCurrentTime());
      if (disposalBuilding instanceof WasteDisposalBuilding) {
        WasteDisposalBuilding wasteDisposal = (WasteDisposalBuilding) disposalBuilding;
        int rate = wasteDisposal.getDisposalRateFor(wasteType);
        int timeSteps = wasteDisposal.getDisposalTimeStepsFor(wasteType);

        logger.log("[waste processing]: " + disposalBuilding.getName() + " will process " +
            wasteType.getName() + " at a rate of " + rate + " units per " +
            timeSteps + " time steps");
      }
    }
  }

  /**
   * Converts the simulation to a JSON string.
   * @return converted JSON string.
   */
  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(getGameState());
  }

  /**
   * Saves the current simulation state to a file.
   *
   * @param fileName the name of the file to save to.
   */
  public void save(String fileName) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(fileName)) {
      gson.toJson(getGameState(), writer);
      logger.log("Simulation saved to " + fileName);
    } catch (IOException e) {
      throw new IllegalArgumentException("invalid file name for save" + fileName);
    }
  }

  /**
   * Loads a saved simulation state from a specified file.
   *
   * @param fileName the name of the file containing the saved simulation state.
   * @throws IllegalArgumentException if the file is invalid or cannot be loaded.
   */
  public void load(String fileName) {
    logger.log("Loading " + fileName);

    try (Reader reader = new FileReader(fileName)) {
      loadFromReader(reader);
    } catch (IOException | JsonSyntaxException | JsonIOException e) {
      throw new IllegalArgumentException("Invalid file name for load" + fileName);
      // System.err.println("Error loading file");
    }
  }

  /**
   * Loads a simulation saved in JSON format from a reader.
   *
   * @param reader is the reader to read the JSON from.
   * @throws com.google.gson.JsonSyntaxException when JSON syntax is bad.
   * @throws com.google.gson.JsonIOException     when JSON IO error.
   */
  void loadFromReader(Reader reader) {
    Gson gson = new Gson();

    String json;
    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
      json = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException | UncheckedIOException e) {
      throw new IllegalArgumentException("Error reading from reader",
          (e instanceof UncheckedIOException ? e.getCause() : e));
    }

    JsonObject state = gson.fromJson(new StringReader(json), JsonObject.class);

    // load fields in simulation
    this.currentTime = getJsonField(state, "currentTime", 0);
    this.finished = getJsonField(state, "finished", false);
    this.nextOrderNum = getJsonField(state, "nextOrderNum", 0);
    this.verbosity = getJsonField(state, "verbosity", 0);
    this.boardWidth = getJsonField(state, "boardWidth", 480);
    this.boardHeight = getJsonField(state, "boardHeight", 270);

    // set tile map dimensions
    // this.setTileMapDimensions(this.boardWidth, this.boardHeight);

    ConfigData configData = JsonLoader.loadConfigDataFromReader(new StringReader(json));
    this.world = WorldBuilder.buildWorld(configData, this, this.boardWidth, this.boardHeight);

    // Restore connections
    JsonElement connectionsElement = state.get("connections");
    if (connectionsElement != null && !connectionsElement.isJsonNull()) {
      JsonArray connectionsArray = connectionsElement.getAsJsonArray();
      for (JsonElement element : connectionsArray) {
        JsonObject connection = element.getAsJsonObject();
        String source = connection.get("source").getAsString();
        String destination = connection.get("destination").getAsString();

        try {
          connectBuildings(source, destination);
        } catch (IllegalArgumentException e) {
          // Log error but continue with other connections
          if (verbosity > 0) {
            logger.log("Failed to establish connection from " + source +
                " to " + destination + ": " + e.getMessage());
          }
        }
      }
    }

    JsonElement requestsElement = state.get("requests");
    JsonArray requestsArray;
    if (requestsElement == null) {
      requestsArray = new JsonArray();
    } else {
      if (requestsElement.isJsonNull()) {
        throw new IllegalArgumentException("Array field 'requests' cannot explicitly be null");
      }
      requestsArray = requestsElement.getAsJsonArray();
    }
    buildRequests(requestsArray);
    JsonElement deliveriesElement = state.get("deliveries");
    if (deliveriesElement != null) {
      buildDeliveries(deliveriesElement.getAsJsonArray());
    }
  }

  /**
   * Reconstructs the list of deliveries from a JSON array.
   * Each delivery includes its source, destination, item, quantity,
   * current delivery time, path position, and current coordinate.
   *
   * @param jsonDeliveries the JSON array containing delivery information
   */
  private void buildDeliveries(JsonArray jsonDeliveries) {
    for (JsonElement element : jsonDeliveries) {
      JsonObject ob = element.getAsJsonObject();
      Building source = world.getBuildingFromName(ob.get("source").getAsString());
      Building destination = world.getBuildingFromName(ob.get("destination").getAsString());
      Item item = new Item(ob.get("item").getAsString());
      int quantity = ob.get("quantity").getAsInt();
      int deliveryTime = ob.get("deliveryTime").getAsInt();
      int pathIndex = ob.get("pathIndex").getAsInt();
      int stepIndex = ob.get("stepIndex").getAsInt();
      Coordinate currentCoordinate = new Coordinate(ob.get("x").getAsInt(),
          ob.get("y").getAsInt());
      Delivery delivery = new Delivery(source, destination, item, quantity, deliveryTime, pathIndex, stepIndex,
          currentCoordinate);
      deliverySchedule.addDelivery(delivery);
    }
  }

  /**
   * Get the value of a field as an integer.
   *
   * @param obj is the JSON object to get the value from.
   * @param key is the name of the field.
   * @param def is the default value if the field is missing.
   * @return the value got from the JSON object.
   * @throws IllegalArgumentException if field value is not an integer.
   */
  private static int getJsonField(JsonObject obj, String key, int def) {
    JsonElement element = obj.get(key);
    if (element == null) {
      return def;
    }
    if (element.isJsonNull()) {
      throw new IllegalArgumentException("Integer field '" + key + "' cannot explicitly be null");
    }
    try {
      return element.getAsInt();
    } catch (UnsupportedOperationException | NumberFormatException e) {
      throw new IllegalArgumentException("Invalid value for integer field '" + key + "'", e);
    }
  }

  /**
   * Get the value of a field as a boolean.
   *
   * @param obj is the JSON object to get the value from.
   * @param key is the name of the field.
   * @param def is the default value if the field is missing.
   * @return the value got from the JSON object.
   * @throws IllegalArgumentException if field value is not a boolean.
   */
  private static boolean getJsonField(JsonObject obj, String key, boolean def) {
    JsonElement element = obj.get(key);
    if (element == null) {
      return def;
    }
    if (element.isJsonNull()) {
      throw new IllegalArgumentException("Boolean field '" + key + "' cannot explicitly be null");
    }
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
      throw new IllegalArgumentException("Field '" + key + "' must be a boolean literal");
    }
    return element.getAsBoolean();
  }

  /**
   * Reconstructs requests from a JSON array and assigns them to buildings.
   *
   * @param requestsArray the JSON array containing saved requests.
   * @throws IllegalArgumentException if any referenced recipe or building is
   *                                  missing.
   */
  public void buildRequests(JsonArray requestsArray) {
    if (requestsArray.isEmpty()) {
      return;
    }
    List<Request> requests = new ArrayList<>();

    for (JsonElement element : requestsArray) {
      JsonObject json = element.getAsJsonObject();

      int orderNum = json.get("orderNum").getAsInt();
      String itemName = json.get("item").getAsString();
      Item item = new Item(itemName);

      String recipeName = json.get("recipe").getAsString();
      Recipe recipe = world.getRecipeFromName(recipeName);
      if (recipe == null) {
        throw new IllegalArgumentException("Recipe not found for item: " + recipeName);
      }

      String producerName = json.get("producer").getAsString();
      Building producer = world.getBuildingFromName(producerName);
      if (producer == null) {
        throw new IllegalArgumentException("Producer building not found: " + producerName);
      }

      Building deliverTo = null;
      if (!json.get("deliverTo").getAsString().equals("null")) {
        String deliverToName = json.get("deliverTo").getAsString();
        deliverTo = world.getBuildingFromName(deliverToName);
        if (deliverTo == null) {
          throw new IllegalArgumentException("DeliverTo building not found: " + deliverToName);
        }
      }

      int remainingSteps = json.get("remainingSteps").getAsInt();
      String status = json.get("status").getAsString();

      Request request = new Request(orderNum, item, recipe, producer, deliverTo);
      // System.out.println("DEBUG: Parsing request from JSON, orderNum=" + orderNum);
      request.setRemainingSteps(remainingSteps);
      request.setStatus(status);

      requests.add(request);
    }
    assignRequestsToBuildings(requests);
  }

  /**
   * Assigns requests to the appropriate buildings.
   *
   * @param requests The list of requests.
   */
  private void assignRequestsToBuildings(List<Request> requests) {
    for (Request request : requests) {
      Building targetBuilding = request.getProducer();

      if (request.getStatus().equals("current")) {
        targetBuilding.setCurrentRequest(request);
      } else {
        targetBuilding.appendPendingRequest(request);
      }
    }
  }

  /**
   * Updates the tile map with given tile type and coordinate.
   *
   * @param location is the new coordinate.
   * @param tileType is the tileType to be updated.
   */
  public void updateTileMap(Coordinate location, TileType tileType) {
    world.tileMap.setTileType(location, tileType);
  }

  /**
   * Sets the dimensions of the tile map.
   *
   * @param width  is the width of the board.
   * @param height is the height of the board.
   */
  public void setTileMapDimensions(int width, int height) {
    world.setTileMapDimensions(width, height);
  }

  /**
   * Gets the location of a building from the location map.
   *
   * @param buildingName is the name of building to look up.
   * @return the corresponding location.
   */
  public Coordinate getBuildingLocation(String buildingName) {
    return world.getBuildingFromName(buildingName).getLocation();
  }

  /**
   * Checks what is on the specific coordinate.
   *
   * @param coordinate is the coordinate to be checked.
   * @return TileType on that coordinate.
   */
  public TileType checkTile(Coordinate coordinate) {

    return world.tileMap.getTileType(coordinate);
  }

  /**
   * Gets the world managed by this simulation.
   *
   * @return the world object.
   */
  public World getWorld() {
    return world;
  }

  /**
   * Establishes connections between buildings based on the provided connection
   * data.
   *
   * @param connections is a list of connection data transfer objects.
   * @throws IllegalArgumentException if buildings cannot be connected.
   */
  public void establishConnections(List<ConnectionDTO> connections) {
    if (connections == null || connections.isEmpty()) {
      return;
    }

    for (ConnectionDTO connection : connections) {
      try {
        connectBuildings(connection.getSource(), connection.getDestination());
      } catch (IllegalArgumentException e) {
        // log error but continue with other connections
        if (verbosity > 0) {
          logger.log("Failed to establish connection from " + connection.getSource() +
              " to " + connection.getDestination() + ": " + e.getMessage());
        }
      }
    }
  }

  /**
   * Attempts to connect two buildings using the shortest valid path on the map.
   * If a valid path already exists in the cache, it is reused. Otherwise, a new
   * path is found and added to the path list and the tile map.
   * Also verifies that source building's output can be properly used by
   * destination building.
   *
   * @param srcBuilding is the source building to connect.
   * @param dstBuilding is the destination building to connect.
   * @return connected `Path` instance if the buildings are successfully
   *         connected.
   * @throws IllegalArgumentException if no valid path can be found between the
   *                                  buildings or if the buildings are not
   *                                  compatible.
   */
  public Path connectBuildings(Building srcBuilding, Building dstBuilding) {
    // Check recipe compatibility between source and destination buildings
    if (!areBuildingsCompatible(srcBuilding, dstBuilding)) {
      throw new IllegalArgumentException(
          "Cannot connect " + srcBuilding.getName() + " to " + dstBuilding.getName() +
              ": Source output cannot be used as input for destination.");
    }

    Coordinate src = srcBuilding.getLocation();
    Coordinate dst = dstBuilding.getLocation();
    for (Path p : pathList) {
      if (p.isMatch(src, dst)) {
        // Update destination building's sources when connecting
        List<Building> sources = new ArrayList<>(dstBuilding.getSources());
        if (!sources.contains(srcBuilding)) {
          sources.add(srcBuilding);
          dstBuilding.updateSources(sources);
        }
        return p;
      }
    }

    Path path = PathFinder.findPath(src, dst, world.tileMap);
    if (path == null) {
      throw new IllegalArgumentException(
          "Cannot connect " + srcBuilding.getName() + " to " + dstBuilding.getName() + ": No valid path.");
    } else {
      // add the path to the cache
      pathList.add(path);

      // add the path to the tileMap
      world.tileMap.addPath(path);

      // Update destination building's sources
      List<Building> sources = new ArrayList<>(dstBuilding.getSources());
      if (!sources.contains(srcBuilding)) {
        sources.add(srcBuilding);
        dstBuilding.updateSources(sources);
      }
    }
    return path;
  }

  /**
   * Checks if the source building's output is compatible with the destination
   * building.
   * For mines, checks if the mined resource can be used as an ingredient by the
   * destination.
   * For factories, checks if any factory output can be used as an ingredient by
   * the destination.
   * For storage buildings, checks if the source produces what the storage
   * building can store.
   *
   * @param srcBuilding The source building
   * @param dstBuilding The destination building
   * @return true if the buildings are compatible, false otherwise
   */
  private boolean areBuildingsCompatible(Building srcBuilding, Building dstBuilding) {
    // Mines, Factories and Storage buildings have different compatibility checks
    if (srcBuilding instanceof MineBuilding) {
      MineBuilding mineBuilding = (MineBuilding) srcBuilding;
      Item mineOutput = mineBuilding.getResource();

      // If destination is a factory, check if mine output is used in any recipe
      if (dstBuilding instanceof FactoryBuilding) {
        FactoryBuilding factory = (FactoryBuilding) dstBuilding;
        List<Recipe> recipes = factory.getFactoryType().getRecipes();

        for (Recipe recipe : recipes) {
          if (recipe.getIngredients().containsKey(mineOutput)) {
            return true;
          }
        }
        return false; // Mine output not used in any factory recipe
      }
      // If destination is a storage building, check if it can store the mine's output
      else if (dstBuilding instanceof StorageBuilding) {
        StorageBuilding storage = (StorageBuilding) dstBuilding;
        return storage.getStorageItem().equals(mineOutput);
      }
    }
    // For factory as source, check if any output can be used by destination
    else if (srcBuilding instanceof FactoryBuilding) {
      FactoryBuilding factoryBuilding = (FactoryBuilding) srcBuilding;
      List<Recipe> recipes = factoryBuilding.getFactoryType().getRecipes();

      if (dstBuilding instanceof FactoryBuilding) {
        FactoryBuilding destFactory = (FactoryBuilding) dstBuilding;
        List<Recipe> destRecipes = destFactory.getFactoryType().getRecipes();

        // Check if any source factory output is used as ingredient in any destination
        // factory recipe
        for (Recipe srcRecipe : recipes) {
          Item output = srcRecipe.getOutput();
          for (Recipe destRecipe : destRecipes) {
            if (destRecipe.getIngredients().containsKey(output)) {
              return true;
            }
          }
        }
        return false; // No compatible recipes found
      }
      // If destination is a storage building, check if it can store any factory
      // output
      else if (dstBuilding instanceof StorageBuilding) {
        StorageBuilding storage = (StorageBuilding) dstBuilding;
        Item storageItem = storage.getStorageItem();

        for (Recipe recipe : recipes) {
          if (recipe.getOutput().equals(storageItem)) {
            return true;
          }
        }
        return false; // No compatible output found for storage
      }
    }

    // All other connections are allowed
    return true;
  }

  /**
   * Disconnects two buildings by removing the path between them.
   * If the path is in use, it cannot be removed.
   *
   * @param srcBuilding is the source building to disconnect.
   * @param dstBuilding is the destination building to disconnect.
   * @return true if the buildings are successfully disconnected, false
   *         otherwise.
   */
  public boolean disconnectBuildings(Building srcBuilding, Building dstBuilding) {
    Coordinate src = srcBuilding.getLocation();
    Coordinate dst = dstBuilding.getLocation();

    Iterator<Path> iterator = pathList.iterator();
    int index = 0;

    while (iterator.hasNext()) {
      Path path = iterator.next();

      if (path.isMatch(src, dst)) {
        if (deliverySchedule.checkUsingPath(path)) {
          throw new IllegalArgumentException("The path is in use by delivery and cannot be removed.");
        }
        if (checkNewTileReuse(index)) {
          throw new IllegalArgumentException("The path is in use by other path and cannot be removed.");
        }
        removePath(path, pathList);
        iterator.remove();

        return true;
      }
      index++;
    }

    throw new IllegalArgumentException("The path does not exist.");
  }

  public boolean checkNewTileReuse(int pathIndex) {
    List<Coordinate> allSteps = pathList.get(pathIndex).getSteps();
    Set<Coordinate> newTiles = pathList.get(pathIndex).getNewTiles();

    for (int i = 0; i < pathList.size(); i++) {
      if (i <= pathIndex) {
        continue;
      }
      Path p = pathList.get(i);
      List<Coordinate> otherSteps = p.getSteps();

      for (int j = 0; j < allSteps.size(); j++) {
        Coordinate step = allSteps.get(j);
        if (newTiles.contains(step) && otherSteps.contains(step)) {
          if (j + 1 < allSteps.size() && otherSteps.contains(allSteps.get(j + 1))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public void removePath(Path path, List<Path> allPaths) {
    List<Coordinate> steps = path.getSteps();

    for (int i = 0; i < steps.size() - 1; i++) {
      Coordinate from = steps.get(i);
      Coordinate to = steps.get(i + 1);
      int dir = getDirection(from, to);
      int oppDir = (dir + 2) % 4;

      // if (world.tileMap.getTileType(from) ==
      // TileType.BUILDING||world.tileMap.getTileType(to) == TileType.BUILDING) {
      // continue;
      // }
      boolean reused = false;
      for (Path p : allPaths) {
        if (p == path)
          continue;
        List<Coordinate> otherSteps = p.getSteps();
        for (int j = 0; j < otherSteps.size() - 1; j++) {
          if ((otherSteps.get(j).equals(from) && otherSteps.get(j + 1).equals(to)) ||
              (otherSteps.get(j).equals(to) && otherSteps.get(j + 1).equals(from))) {
            reused = true;
            break;
          }
        }
        if (reused)
          break;
      }

      if (!reused) {
        if (world.tileMap.getTileType(from) != TileType.BUILDING) {
          world.tileMap.getFlows(from)[dir] = 0;
        }
        if (world.tileMap.getTileType(to) != TileType.BUILDING) {
          world.tileMap.getFlows(to)[oppDir] = 0;
        }

        if (isZeroFlow(world.tileMap.getFlows(from))) {
          world.tileMap.setTileType(from, TileType.ROAD);
        }
        // if (isZeroFlow(world.tileMap.getFlows(to))) {
        // world.tileMap.setTileType(to, TileType.ROAD);
        // }
      }
    }
  }

  public boolean isZeroFlow(int[] flows) {
    for (int f : flows) {
      if (f != 0)
        return false;
    }
    return true;
  }

  public int getDirection(Coordinate from, Coordinate to) {
    int dx = to.getX() - from.getX();
    int dy = to.getY() - from.getY();
    if (dx == 0 && dy == -1)
      return 0; // up
    if (dx == 1 && dy == 0)
      return 1; // right
    if (dx == 0 && dy == 1)
      return 2; // down
    if (dx == -1 && dy == 0)
      return 3; // left
    throw new IllegalArgumentException("Invalid coordinates for direction: " + from + " to " + to);
  }

  /**
   * Attempts to connect two buildings by name using the shortest valid path on
   * the map.
   * If a valid path already exists in the cache, it is reused. Otherwise, a new
   * path is found and added to the path list and the tile map.
   * Also verifies that source building's output can be properly used by
   * destination building.
   *
   * @param sourceName the name of the source building
   * @param destName   the name of the destination building
   * @return true if the buildings are successfully connected
   * @throws IllegalArgumentException if no valid path can be found between the
   *                                  buildings or if they are not recipe
   *                                  compatible
   */
  public boolean connectBuildings(String sourceName, String destName) {
    Building srcBuilding = world.getBuildingFromName(sourceName);
    Building dstBuilding = world.getBuildingFromName(destName);

    if (srcBuilding == null) {
      throw new IllegalArgumentException("Source building '" + sourceName + "' does not exist.");
    }
    if (dstBuilding == null) {
      throw new IllegalArgumentException("Destination building '" + destName + "' does not exist.");
    }

    return connectBuildings(srcBuilding, dstBuilding) != null;
  }

  /**
   * Attempts to disconnect two buildings by name.
   * If the path is in use, it cannot be removed.
   *
   * @param sourceName the name of the source building
   * @param destName   the name of the destination building
   * @return true if the buildings are successfully disconnected, false
   *         otherwise
   */
  public boolean disconnectBuildings(String sourceName, String destName) {
    return disconnectBuildings(world.getBuildingFromName(sourceName), world.getBuildingFromName(destName));
  }
  // public boolean connectBuildings(String sourceName, String destName) {
  // Coordinate src = getBuildingLocation(sourceName);
  // Coordinate dst = getBuildingLocation(destName);
  // for(Path p: pathList){
  // if(p.isMatch(src, dst)){
  // return true;
  // }
  // }
  // Path path = PathFinder.findPath(src, dst, world.tileMap);
  // if (path == null) {
  // throw new IllegalArgumentException("Cannot connect " + sourceName + " to " +
  // destName + ": No valid path.");
  // } else {
  // path.dump();
  // // add the path to the cache
  // pathList.add(path);
  //
  // // add the path to the tileMap
  // world.tileMap.addPath(path);
  // // System.out.println(world.tileMap);
  // }
  // return true;
  // }

  /**
   * Delivers items from one building to another, using drone delivery if
   * possible.
   * Will use drone delivery if both buildings are within range of a drone port
   * that has available drones.
   * Otherwise, use normal road delivery.
   *
   * @param src      the source building
   * @param dst      the destination building
   * @param item     the item to be delivered
   * @param quantity the quantity of the item to be delivered
   * @throws IllegalArgumentException if there is no connection between the source
   *                                  and destination
   */
  public void addDelivery(Building src, Building dst, Item item, int quantity) {
    // check if drone delivery is possible; if so, priority over road
    DronePortBuilding dronePortBuilding = findSuitableDronePort(src, dst);

    if (dronePortBuilding != null) {
      // drone delivery is possible
      DronePort dronePort = dronePortBuilding.getDronePort();
      Drone drone = dronePort.getAvailableDrone();
      if (drone != null) {
        DroneDelivery droneDelivery = new DroneDelivery(dronePort, drone, src, dst, item, quantity);
        deliverySchedule.addDelivery(droneDelivery);

        if (verbosity > 0) {
          logger.log("[drone delivery scheduled]: Using drone from " + dronePortBuilding.getName() +
              " to deliver " + quantity + " " + item.getName() +
              " from " + src.getName() + " to " + dst.getName());
        }
        return;
      }
    }

    // if drone not available, use normal road delivery
    boolean isConnected = false;
    for (int i = 0; i < pathList.size(); i++) {
      if (pathList.get(i).isMatch(src.getLocation(), dst.getLocation())) {
        Delivery d = new Delivery(src, dst, item, quantity, pathList.get(i).getDeliveryTime(), i);
        deliverySchedule.addDelivery(d);
        isConnected = true;
        break;
      }
    }
    if (!isConnected) {
      throw new IllegalArgumentException("building " + src.getName() + " and " + dst.getName() + " are not connected");
    }
  }

  /**
   * Finds a suitable drone port for delivery between two buildings.
   * A suitable drone port must have both buildings within its radius and have
   * available drones.
   *
   * @param src the source building
   * @param dst the destination building
   * @return a suitable drone port building, or null if none is available
   */
  private DronePortBuilding findSuitableDronePort(Building src, Building dst) {
    for (Building building : world.getBuildings()) {
      if (building instanceof DronePortBuilding) {
        DronePortBuilding dronePortBuilding = (DronePortBuilding) building;
        DronePort dronePort = dronePortBuilding.getDronePort();

        if (dronePort.isWithinRadius(src) && dronePort.isWithinRadius(dst) && dronePort.hasAvailableDrone()) {
          return dronePortBuilding;
        }
      }
    }
    return null;
  }

  /**
   * Converts the cached list of paths into a JSON array.
   * Each path is serialized with coordinate steps, flow directions, and new
   * tiles.
   *
   * @return a JsonArray containing all paths in the system
   */
  public JsonArray pathListToJson() {
    JsonArray pathArr = new JsonArray();
    for (Path p : pathList) {
      JsonObject pathObj = new JsonObject();
      String source = getBuildingNameByCoordinate(p.getSteps().getFirst());
      String destination = getBuildingNameByCoordinate(p.getSteps().getLast());
      pathObj.addProperty("source", source);
      pathObj.addProperty("destination", destination);
      pathArr.add(pathObj);
    }
    return pathArr;
  }

  /**
   * Returns building name by coordinate.
   *
   * @return a building name
   */
  public String getBuildingNameByCoordinate(Coordinate coordinate) {
    for (Building building : world.getBuildings()) {
      if (building.getLocation().equals(coordinate)) {
        return building.getName();
      }
    }
    return null;
  }

  /**
   * Returns the current delivery coordinates in the system.
   * Each coordinate represents the current position of a delivery item.
   *
   * @return a list of coordinates representing delivery positions
   */
  public List<Coordinate> getDeliveryCoordinates() {
    return deliverySchedule.getCurrentCoordinates();
  }

  /**
   * Returns the list of all existing paths in the system.
   * Each path represents a connection between two buildings via tiles.
   *
   * @return a list of all established paths
   */
  public List<Path> getPathList() {
    return pathList;
  }

  /**
   * Saves the current simulation state to the database for a given user.
   *
   * This method serializes the internal game state as a JSON string and stores
   * it in the `sessions` table of the SQLite database, associated with the
   * specified user ID.
   *
   * @param userId the identifier of the user whose session is being saved.
   */
  public void saveToDB(String userId) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String jsonStr = gson.toJson(getGameState());
    SessionDAO.saveSession(userId, jsonStr);
    logger.log("Simulation saved to DB for user " + userId);
  }

  /**
   * Loads a simulation state from the database for a given user.
   *
   * This method retrieves the JSON string associated with the user ID from
   * the database and reconstructs the simulation state using it.
   *
   * @param userId the identifier of the user whose session is to be loaded.
   * @throws IllegalArgumentException if no saved session is found for the given
   *                                  user ID.
   */
  public void loadFromDB(String userId) {
    String json = SessionDAO.loadSession(userId);
    if (json == null) {
      throw new IllegalArgumentException("No saved session found for user: " + userId);
    }
    Reader reader = new StringReader(json);
    loadFromReader(reader);
    logger.log("Simulation loaded from DB for user " + userId);
  }

  /**
   * Converts the current game state into a JSON object.
   *
   * @return a JsonObject representing the current game state
   */
  public JsonObject getGameState() {
    JsonObject state = new JsonObject();
    state.addProperty("currentTime", currentTime);
    state.addProperty("finished", finished);
    state.addProperty("nextOrderNum", nextOrderNum);
    state.addProperty("verbosity", verbosity);
    state.addProperty("boardWidth", world.getTileMap().getWidth());
    state.addProperty("boardHeight", world.getTileMap().getHeight());

    JsonArray typesArray = new JsonArray();
    for (Type type : world.getTypes()) {
      typesArray.add(type.toJson());
    }
    state.add("types", typesArray);

    JsonArray buildingsArray = new JsonArray();
    for (Building building : world.getBuildings()) {
      buildingsArray.add(building.toJson());
    }
    state.add("buildings", buildingsArray);

    JsonArray recipesArray = new JsonArray();
    for (Recipe recipe : world.getRecipes()) {
      recipesArray.add(recipe.toJson());
    }
    state.add("recipes", recipesArray);

    JsonArray requestArray = new JsonArray();
    for (Building building : world.getBuildings()) {
      if (building.getCurrentRequest() != null) {
        requestArray.add(building.getCurrentRequest().toJson());
      }
      if (!building.getPendingRequest().isEmpty()) {
        building.getPendingRequest().forEach(request -> requestArray.add(request.toJson()));
      }
    }
    state.add("requests", requestArray);
    // state.add("tileMap", world.tileMap.toJson());
    state.add("connections", pathListToJson());
    state.add("deliveries", deliverySchedule.toJson());
    return state;
  }

  /**
   * Attempts to mark a building for removal.
   * If the building can be removed immediately, it is removed; otherwise, it is
   * marked for pending removal.
   *
   * @param building the building to remove
   * @return true if the building was removed immediately, false if it was marked
   *         for pending removal
   */
  public boolean removeBuilding(Building building) {
    if (building.canBeRemovedImmediately()) {
      removeConnectionsForBuilding(building);
      world.removeBuildingFromWorld(building);
      if (verbosity > 0) {
        logger.log("Building '" + building.getName() + "' has been removed.");
      }
      onBuildingRemoved.invoke(building);
      return true;
    } else {
      building.markForRemoval();
      if (verbosity > 0) {
        logger.log("Building '" + building.getName() + "' has been marked for removal. " +
            "It will be removed once all pending operations complete.");
      }
      return false;
    }
  }

  /**
   * Attempts to mark a building for removal by name.
   * If the building can be removed immediately, it is removed; otherwise, it is
   * marked for pending removal.
   *
   * @param buildingName the name of the building to remove
   * @return true if the building was removed immediately, false if it was marked
   *         for pending removal
   * @throws IllegalArgumentException if the building doesn't exist
   */
  public boolean removeBuilding(String buildingName) {
    if (!world.hasBuilding(buildingName)) {
      throw new IllegalArgumentException("Building '" + buildingName + "' does not exist.");
    }

    Building building = world.getBuildingFromName(buildingName);
    return removeBuilding(building);
  }

  /**
   * Checks if any buildings marked for removal can now be removed.
   */
  public void checkPendingRemovals() {
    List<Building> buildings = world.getBuildings();
    List<Building> removedBuildings = new ArrayList<>();
    for (Building building : buildings) {
      if (building.isPendingRemoval() && building.canBeRemovedImmediately()) {
        removeConnectionsForBuilding(building);
        removedBuildings.add(building);
        if (verbosity > 0) {
          logger
              .log("Building '" + building.getName() + "' has completed all pending operations and has been removed.");
        }
      }
    }
    for (Building building : removedBuildings) {
      world.removeBuildingFromWorld(building);
      onBuildingRemoved.invoke(building);
    }
  }

  /**
   * Removes all connections to and from a building.
   *
   * @param building the building whose connections should be removed
   */
  private void removeConnectionsForBuilding(Building building) {
    List<Building> connectedBuildings = new ArrayList<>();
    // find all buildings that this building is a source for
    for (Building otherBuilding : world.getBuildings()) {
      if (otherBuilding != building && otherBuilding.getSources().contains(building)) {
        connectedBuildings.add(otherBuilding);
      }
    }
    // disconnect this building from each connected building
    for (Building otherBuilding : connectedBuildings) {
      try {
        disconnectBuildings(building, otherBuilding);
      } catch (IllegalArgumentException e) {
        if (verbosity > 0) {
          logger.log("Failed to disconnect " + building.getName() + " from " +
              otherBuilding.getName() + ": " + e.getMessage());
        }
      }
    }
    // also disconnect all buildings that are sources for this building
    for (Building source : new ArrayList<>(building.getSources())) {
      try {
        disconnectBuildings(source, building);
      } catch (IllegalArgumentException e) {
        if (verbosity > 0) {
          logger.log("Failed to disconnect " + source.getName() + " from " +
              building.getName() + ": " + e.getMessage());
        }
      }
    }
  }
}
