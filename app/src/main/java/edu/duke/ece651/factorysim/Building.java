package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a building in the simulation.
 */
public abstract class Building {
  private final String name;
  private final List<Building> sources;
  private final Simulation simulation;

  private HashMap<Item, Integer> storage;
  private Request currentRequest = null;
  private List<Request> pendingRequests;
  private RequestPolicy requestPolicy;
  private SourcePolicy sourcePolicy;

  /**
   * Constructs a basic building with empty storage.
   * 
   * @param name    is the name of the building.
   * @param sources is the list of buildings where this building can get
   *                ingredients from.
   * @throws IllegalArgumentException if the name is not valid.
   */
  protected Building(String name, List<Building> sources, Simulation simulation) {
    if (Utils.isNameValid(name) == false) {
      throw new IllegalArgumentException(
          "Building name cannot contain " + Utils.notAllowedInName + ", but is: " + name);
    }
    this.name = name;
    this.sources = sources;
    this.simulation = simulation;
    this.storage = new HashMap<>();
    this.pendingRequests = new LinkedList<>();
    this.requestPolicy = simulation.getRequestPolicy(name);
    this.sourcePolicy = simulation.getSourcePolicy(name);
  }

  public Map<Item, Integer> getStorage() {
    return storage;
  }

  /**
   * Gets the simulation.
   * 
   * @return the simulation.
   */
  public Simulation getSimulation() {
    return simulation;
  }

  /**
   * Gets the recipe for a given item.
   * 
   * @param item is the item to get the recipe for.
   * @return the recipe for the item.
   */
  public Recipe getRecipeForItem(Item item) {
    return simulation.getRecipeForItem(item);
  }

  /**
   * Gets the name of the building.
   * 
   * @return name of the building.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the sources of the building.
   * 
   * @return list of sources of the building.
   */
  public List<Building> getSources() {
    return sources;
  }

  /**
   * Updates the sources of the building.
   * 
   * @param newSources is the new list of sources.
   */
  public void updateSources(List<Building> newSources) {
    sources.clear();
    sources.addAll(newSources);
  }

  /**
   * Gets the current storage number of an item.
   * 
   * @param item is the item to be checked.
   * @return -1 if the requested item is not in storage, otherwise the current
   *         storage number of that item.
   */
  public int getStorageNumberOf(Item item) {
    if (storage.containsKey(item) == false) {
      return -1;
    } else {
      return storage.get(item);
    }
  }

  /**
   * Update the storage by adding things in.
   * 
   * @param item     is the item to be updated.
   * @param quantity is the number of the item to be added into storage.
   */
  public void addToStorage(Item item, int quantity) {
    int existingNum = 0;
    if (storage.containsKey(item)) {
      existingNum = storage.get(item);
    }
    storage.put(item, existingNum + quantity);
  }

  /**
   * Update the storage by taking things out.
   * 
   * @param item     is the item to be updated.
   * @param quantity is the number of the item to be taken out of storage.
   * @throws IllegalArgumentException if the item does not exist in storage, or
   *                                  there isn't enough number to be taken out.
   */
  public void takeFromStorage(Item item, int quantity) {
    if (storage.containsKey(item) == false) {
      throw new IllegalArgumentException(
          "Cannot take " + item.getName() + " out of " + name + "'s storage, because it's not in stock.");
    }
    int storageNum = storage.get(item);
    if (storageNum < quantity) {
      throw new IllegalArgumentException("Cannot take " + quantity + " " + item.getName() + " out of " + name
          + "'s storage, because there isn't enough stock.");
    }
    int updatedStorageNum = storageNum - quantity;
    // if there is no storage left, delete the item from storage
    if (updatedStorageNum == 0) {
      storage.remove(item);
    } else {
      storage.put(item, updatedStorageNum);
    }
  }

  /**
   * Delivers things to another building.
   * 
   * @param destination is the destination building.
   * @param item        is the item to be delivered.
   * @param quantity    is the quantity of item to be delivered.
   */
  public void deliverTo(Building destination, Item item, int quantity) {
    destination.addToStorage(item, quantity);
  }

  /**
   * Add a request to the beginning of the pending request list.
   * This is useful mainly used to maintain the order that older requests are
   * towards the end of the list.
   * NOTE: this method only adds the request, it doesn't request ingredients from
   * sources like `addRequest`.
   *
   * @param request the request to be added.
   */
  public void prependPendingRequest(Request request) {
    pendingRequests.addFirst(request);
  }

  /**
   * Add a request to the end of the pending request list.
   * This is used for reconstructing the pending request list with the original
   * order.
   * NOTE: this method only adds the request, it doesn't request ingredients from
   * sources like `addRequest`.
   *
   * @param request the request to be added.
   */
  public void appendPendingRequest(Request request) {
    pendingRequests.addLast(request);
  }

  /**
   * Add a new request to the request queue.<br/>
   * The building MUST be able to process this request (because `Simulation` need
   * to log sub-request details).
   * NOTE: if you only want to add a request without requesting ingredients from
   * sources, use `addPendingRequest`
   * instead.
   *
   * @param request the request to be added.
   * @throws IllegalArgumentException when the building cannot process this
   *                                  request.
   */
  public void addRequest(Request request) {
    // notify simulation of source selection
    simulation.onSourceSelected(this, sourcePolicy, request.getItem());

    // Add request as a pending request
    prependPendingRequest(request);

    // Send out requests for ingredients
    requestMissingIngredients(request.getRecipe());
  }

  public void setCurrentRequest(Request request) {
    currentRequest = request;
  }

  /**
   * Checks if the building is processing a request currently.
   *
   * @return true if the building is currently processing a request, false
   *         otherwise.
   */
  public boolean isProcessing() {
    return currentRequest != null; // If there's a current request, it means the building is processing it
  }

  /**
   * Checks if the factory/building has finished processing all requests.
   *
   * @return true if there are no active requests and nothing is being processed,
   *         false otherwise.
   */
  public boolean isFinished() {
    // Building is "finished" when there's no current request processing AND there's
    // no pending requests
    return !isProcessing() && pendingRequests.isEmpty();
  }

  /**
   * Steps the building forward in time.
   * Updates the request and source policy for the building.
   */
  public void step() {
    requestPolicy = simulation.getRequestPolicy(name);
    sourcePolicy = simulation.getSourcePolicy(name);

    processRequest();
  }

  /**
   * Request processing routine.
   * If there's no current request, fetch one using the current policy, then
   * process it by one step.
   * Otherwise, just keep processing the existing current request.<br/>
   * NOTE: This method is called by `step` and should not be invoked manually
   * somewhere else.
   */
  protected void processRequest() {
    // if the building is processing a request, work on the current one
    if (isProcessing()) {
      boolean isRequestFinished = currentRequest.process();
      if (isRequestFinished) {
        finishCurrentRequest();
      }
    }
    // else, try to fetch the next one and work on it
    else if (pendingRequests.isEmpty() == false) {
      // Select a request based on the current policy
      Request selectedRequest = requestPolicy.selectRequest(this, pendingRequests);
      selectedRequest.setStatus("current");
      Recipe selectedRecipe = selectedRequest.getRecipe();

      // Notify simulation a request has been selected
      simulation.onRecipeSelected(this, requestPolicy, pendingRequests, selectedRequest);

      // Start processing request of has all the ingredients for it
      if (hasAllIngredientsFor(selectedRecipe)) {
        consumeIngredientsFor(selectedRecipe);
        pendingRequests.remove(selectedRequest);
        currentRequest = selectedRequest;
      }
    }
  }

  // // A copy of the old `processRequest` that send out sub-requests to sources
  // when request is selected
  // protected void processRequest() {
  // // if the building is processing a request, work on the current one
  // if (isProcessing()) {
  // boolean isRequestFinished = currentRequest.process();
  // if (isRequestFinished) {
  // finishCurrentRequest();
  // }
  // }
  // // else, try to fetch the next one and work on it
  // else if (pendingRequests.isEmpty() == false) {
  // Request selectedRequest = requestPolicy.popRequest(this, pendingRequests);
  // Recipe selectedRecipe = selectedRequest.getRecipe();
  // if (hasAllIngredientsFor(selectedRecipe)) {
  // consumeIngredientsFor(selectedRecipe);
  // } else {
  // HashMap<Item, Integer> missingIngredients =
  // findMissingIngredients(selectedRecipe);
  // requestMissingIngredients(missingIngredients);
  // }
  // }
  // }

  /**
   * Checks if the things in storage are enough to produce the output of a recipe.
   * 
   * @param recipe is the recipe to be checked.
   * @return true if the things in storage are enough, false otherwise.
   */
  public boolean hasAllIngredientsFor(Recipe recipe) {
    for (Item item : recipe.getIngredients().keySet()) {
      int numNeeded = recipe.getIngredients().get(item);
      int numInStorage = getStorageNumberOf(item);
      // num in storage will be -1 if item not exists in storage
      if (numInStorage < numNeeded) {
        return false;
      }
    }
    return true;
  }

  /**
   * Consumes the ingredients in storage for a given recipe.
   * Precondition: hasAllIngredientsFor(recipe) == true
   * 
   * @param recipe is the recipe to be consumed.
   */
  public void consumeIngredientsFor(Recipe recipe) {
    for (Item item : recipe.getIngredients().keySet()) {
      takeFromStorage(item, recipe.getIngredients().get(item));
    }
  }

  /**
   * Finishes the current request by adjusting relavant statues.
   */
  public void finishCurrentRequest() {
    // add the output item to storage
    Item output = currentRequest.getItem();
    addToStorage(output, 1);
    // if the request is not user request (has deliverTo destination building),
    // deliver the output item
    if (currentRequest.isUserRequest() == false) {
      // deliver
      Building destinationBuilding = currentRequest.getDeliverTo();
      deliverTo(destinationBuilding, output, 1);
      simulation.onIngredientDelivered(currentRequest.getItem(), destinationBuilding, this); // notify simulation

      // update our own storage
      takeFromStorage(output, 1);
    } else {
      // indicate simulation a user request is completed
      simulation.onRequestCompleted(currentRequest);
    }
    currentRequest = null;
  }

  /**
   * Finds the missing ingredients item and corresponding quantity for a given
   * recipe, considering current building storage.
   * Precondition: hasAllIngredientsFor(recipe) == false, thus there must be some
   * item whose number in storage is smaller than number needed in recipe
   *
   * @param recipe is the recipe for reference.
   * @return a list of missing ingredients.
   */
  public List<Tuple<Item, Integer>> findMissingIngredients(Recipe recipe) {
    List<Tuple<Item, Integer>> ingredients = new ArrayList<>();
    for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
      int need = entry.getValue();
      int available = storage.getOrDefault(entry.getKey(), 0);
      if (need > available) {
        ingredients.add(new Tuple<>(entry.getKey(), need - available));
      }
    }
    return ingredients;
  }

  /**
   * Requests missing ingredients from sources.
   * NOTE: this is where the source policy takes place.
   *
   * @param recipe is the recipe to check for missing ingredients.
   * @throws IllegalArgumentException if the sources of the building are not
   *                                  enough to give missing items.
   */
  public void requestMissingIngredients(Recipe recipe) {
    List<Tuple<Item, Integer>> missingIngredients = findMissingIngredients(recipe);
    for (int index = 0; index < missingIngredients.size(); index++) {
      Tuple<Item, Integer> entry = missingIngredients.get(index);
      Item item = entry.first();
      int numNeeded = entry.second();

      List<Tuple<Building, Integer>> sourceWithScores = new ArrayList<>();

      // use source policy to select a source
      List<Building> availableSources = getAvailableSourcesForItem(item);
      Building selectedSource = sourcePolicy.selectSource(item, availableSources,
          (source, score) -> sourceWithScores.add(new Tuple<>(source, score)));
      if (selectedSource == null) {
        throw new IllegalArgumentException("No source can produce the item " + item.getName());
      }
      Recipe recipeNeeded = simulation.getRecipeForItem(item);

      // notify simulation an ingredient source is selected
      simulation.onIngredientSourceSelected(this, recipe.getOutput(), index,
          recipeNeeded.getOutput(), sourceWithScores, selectedSource);

      // create sub-requests for numNeeded times for this item
      for (int i = 0; i < numNeeded; i++) {
        int orderNum = simulation.getOrderNum(); // this function automatically proceed the next order num by 1
        Request subRequest = new Request(orderNum, item, recipeNeeded, selectedSource, this);
        simulation.onIngredientAssigned(item, selectedSource, this);
        selectedSource.addRequest(subRequest);
      }
    }
  }

  /**
   * Finds the missing ingredients item and corresponding quantity for a given
   * recipe, considering current building storage.
   * Precondition: hasAllIngredientsFor(recipe) == false, thus there must be some
   * item whose number in storage is smaller than number needed in recipe
   *
   * @return the hashmap for missing ingredients.
   */
  public HashMap<Item, Integer> findMissingIngredientsAsHashMap(Recipe recipe) {
    return new HashMap<>(findMissingIngredients(recipe).stream()
        .collect(Collectors.toMap(
            Tuple::first,
            Tuple::second,
            (t1, t2) -> t2 // Safety precaution in case there's a duplicated item
                           // (but it's impossible when this method was first written
                           // because `Recipe` uses a `LinkedHashMap`)
        )));
  }

  /**
   * Requests missing ingredients from sources.
   * NOTE: this is where the source policy takes place.
   *
   * @param missingIngredients is the hashmap for missing ingredients.
   * @throws IllegalArgumentException if the sources of the building are not
   *                                  enough to give missing items.
   */
  // NOTE: this method is here to prevent existing tests from breaking
  public void requestMissingIngredientsFromHashMap(HashMap<Item, Integer> missingIngredients) {
    List<Tuple<Item, Integer>> list = new ArrayList<>();
    for (Map.Entry<Item, Integer> ingredient : missingIngredients.entrySet()) {
      list.add(new Tuple<>(ingredient.getKey(), ingredient.getValue()));
    }
    requestMissingIngredients(new Recipe(new Item(""), missingIngredients, 0));
  }

  // /**
  // * Finds the missing ingredients item and corresponding quantity for a given
  // * recipe, considering current building storage.
  // * Precondition: hasAllIngredientsFor(recipe) == false, thus there must be
  // some
  // * item whose number in storage is smaller than number needed in recipe
  // *
  // * @return recipe is the recipe for reference.
  // * @return the hashmap for missing ingredients.
  // */
  // public HashMap<Item, Integer> findMissingIngredients(Recipe recipe) {
  // HashMap<Item, Integer> ans = new HashMap<>();
  // for (Item item : recipe.getIngredients().keySet()) {
  // if (storage.containsKey(item) == false) {
  // ans.put(item, recipe.getIngredients().get(item));
  // } else {
  // int numNeeded = recipe.getIngredients().get(item);
  // int numInStorage = storage.get(item);
  // if (numNeeded > numInStorage) {
  // ans.put(item, numNeeded - numInStorage);
  // }
  // }
  // }
  // return ans;
  // }
  //
  // /**
  // * Requests missing ingredients from sources.
  // * NOTE: this is where the source policy takes place.
  // *
  // * @param missingIngredients is the hashmap for missing ingredeints.
  // * @throws IllegalArgumentException if the sources of the building are not
  // * enough to give missing items.
  // */
  // public void requestMissingIngredients(HashMap<Item, Integer>
  // missingIngredients) {
  // for (Item item : missingIngredients.keySet()) {
  // int numNeeded = missingIngredients.get(item);
  // List<Building> availableSources = getAvailableSourcesForItem(item);
  // Building selectedSource = sourcePolicy.selectSource(item, availableSources);
  // if (selectedSource == null) {
  // throw new IllegalArgumentException("No source can produce the item " +
  // item.getName());
  // }
  // Recipe recipeNeeded = simulation.getRecipeForItem(item);
  // // create sub-requests for numNeeded times for this item
  // for (int i = 0; i < numNeeded; i++) {
  // int orderNum = simulation.getOrderNum(); // this function automatically
  // proceed the next order num by 1
  // Request subRequest = new Request(orderNum, item, recipeNeeded,
  // selectedSource, this);
  // selectedSource.addRequest(subRequest);
  //
  // // notify simulation about ingredient assignment
  // simulation.onIngredientAssigned(item, selectedSource, this);
  // }
  // }
  // }

  /**
   * An easy version of request processing routine, assuming that all ingredients
   * required by the request recipe are in the storage.
   *
   **************************************************************************
   * This method is deprecated. Only used for testing in early stages
   * of project.
   **************************************************************************
   */
  void processRequestEasyVersion() {
    // Fetch a new request if there's no current request
    if (currentRequest == null) {
      currentRequest = requestPolicy.selectRequest(this, pendingRequests);

      pendingRequests.remove(currentRequest);

      // Do nothing if no request was fetched
      if (currentRequest == null) {
        return;
      }
      currentRequest.setStatus("current");
    }

    // Process current request by one step
    if (currentRequest.process()) {
      deliverTo(currentRequest.getDeliverTo(), currentRequest.getItem(), 1);

      // Current request is completed, setting it to null to indicate no request
      // processing for the next step
      currentRequest = null;
    }
  }

  /**
   * Gets the number of pending requests of this building.
   * 
   * @return the number of the pending requests.
   */
  public int getNumOfPendingRequests() {
    return pendingRequests.size();
  }

  /**
   * Gets the list of source buildings that can produce a given item.
   * 
   * @return the list of available source buildings.
   */
  public List<Building> getAvailableSourcesForItem(Item item) {
    List<Building> availableSources = new ArrayList<>();
    for (Building source : sources) {
      if (source.canProduce(item)) {
        availableSources.add(source);
      }
    }
    return availableSources;
  }

  /**
   * Gets the sum of the remaining steps of all pending requests and the current
   * request (if any).
   * 
   * @return the sum of all requests' remaining steps.
   */
  public int sumRemainingLatencies() {
    int ans = 0;
    for (Request request : pendingRequests) {
      ans += request.getRemainingSteps();
    }
    if (isProcessing()) {
      ans += currentRequest.getRemainingSteps();
    }
    return ans;
  }

  /**
   * Gets the list of pending requests of this building.
   * 
   * @return the list of pending requests.
   */
  public List<Request> getPendingRequests() {
    return pendingRequests;
  }

  /**
   * Checks if this building can produce a given item.
   *
   * @param item is the item to be checked.
   * @return true if this building can produce this item, false otherwise.
   */
  public abstract boolean canProduce(Item item);

  public abstract JsonObject toJson();

  public List<Request> getPendingRequest() {
    return pendingRequests;
  }

  public Request getCurrentRequest() {
    return currentRequest;
  }

}
