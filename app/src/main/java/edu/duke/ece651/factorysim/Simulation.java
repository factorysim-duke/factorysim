package edu.duke.ece651.factorysim;

import com.google.gson.*;

import java.io.*;
import java.util.*;
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

  private int verbosity;

  private Logger logger;
  private final Map<Coordinate, Map<Coordinate, Path>> pathList = new HashMap<>();
  DeliverySchedule deliverySchedule = new DeliverySchedule();

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
      deliverySchedule.step();
      for (Building building : world.getBuildings()) {
        building.step();
      }

      currentTime++;
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
    if (!building.getClass().equals(FactoryBuilding.class)) {
      return; // Ignore calls from other types of building
    }
    FactoryBuilding factory = (FactoryBuilding) building;

    // Log recipe selection
    logger.log("[recipe selection]: factory " + factory.getName() +
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
    if (!building.getClass().equals(FactoryBuilding.class)) {
      return; // Ignore calls from other types of building
    }
    FactoryBuilding factory = (FactoryBuilding) building;

    // Log source selection
    logger.log("[source selection]: " + factory.getName() +
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
    if (!building.getClass().equals(FactoryBuilding.class)) {
      return; // Ignore calls from other types of building
    }
    FactoryBuilding factory = (FactoryBuilding) building;

    // Log selection detail
    logger.log("[" + factory.getName() + ":" + item.getName() + ":" + index +
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
   * Saves the current simulation state to a file.
   *
   * @param fileName the name of the file to save to.
   */
  public void save(String fileName) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(fileName)) {
      JsonObject state = new JsonObject();

      state.addProperty("currentTime", currentTime);
      state.addProperty("finished", finished);
      state.addProperty("nextOrderNum", nextOrderNum);
      state.addProperty("verbosity", verbosity);

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
        if (/* building.getPendingRequest() != null && */!building.getPendingRequest().isEmpty()) {
          building.getPendingRequest().forEach(request -> requestArray.add(request.toJson()));
        }
      }
      state.add("requests", requestArray);

      state.add("tileMap", world.tileMap.toJson());
      gson.toJson(state, writer);
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

    String json = new BufferedReader(reader)
            .lines().collect(Collectors.joining(System.lineSeparator()));

    ConfigData configData = JsonLoader.loadConfigDataFromReader(new StringReader(json));
    this.world = WorldBuilder.buildWorld(configData, this);

    JsonObject state = gson.fromJson(new StringReader(json), JsonObject.class);

    // load fields in simulation
    this.currentTime = getJsonField(state, "currentTime", 0);
    this.finished = getJsonField(state, "finished", false);
    this.nextOrderNum = getJsonField(state, "nextOrderNum", 0);
    this.verbosity = getJsonField(state, "verbosity", 0);

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

  public World getWorld() {
    return world;
  }

  public boolean connectBuildings(String sourceName, String destName) {
    Coordinate src = getBuildingLocation(sourceName);
    Coordinate dst = getBuildingLocation(destName);
    if(pathList.containsKey(src) && pathList.get(src).containsKey(dst)){
        return true;
    }
    Path path = PathFinder.findPath(src, dst, world.tileMap);
    if (path == null) {
      throw new IllegalArgumentException("Cannot connect " + sourceName + " to " + destName + ": No valid path.");
    } else {
        path.dump();
      // add the path to the cache
      pathList.putIfAbsent(src, new HashMap<>());
      pathList.get(src).put(dst, path);

      // add the path to the tileMap
      world.tileMap.addPath(path);
    }
    return true;
  }


  public void addDelivery(Building src, Building dst, Item item, int quantity) {
    if (pathList.containsKey(src.getLocation()) && pathList.get(src.getLocation()).containsKey(dst.getLocation())) {
        Path path = pathList.get(src.getLocation()).get(dst.getLocation());
        Delivery d=new Delivery(src,dst, item, quantity, path.getDeliveryTime());
        deliverySchedule.addDelivery(d);
    } else {
      throw new IllegalArgumentException("building " + src.getName() + " and " + dst.getName() + " are not connected");
    }
  }
}
