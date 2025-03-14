package edu.duke.ece651.factorysim;

/**
 * Represents a request for an item in the simulation.
 * The request can either come from user to a building, or from a building to
 * another building.
 */
public class Request {
  private final int orderNum;
  private final Item item;
  private final Recipe recipe;
  private final Building producer;
  private final Building deliverTo;
  private boolean isUserRequest;
  private int remainingSteps;

  /**
   * Constructs a request.
   * 
   * @param orderNum      is the order number.
   * @param item          is the item being requested.
   * @param recipe        is the recipe for the requested item.
   * @param producer      is the producer building for the requested item (and
   *                      recipe).
   * @param deliverTo     is the target building to receive the requested item.
   * @param isUserRequest is the boolean to tell if this request is made by user.
   */
  public Request(int orderNum, Item item, Recipe recipe, Building producer, Building deliverTo, boolean isUserRequest) {
    this.orderNum = orderNum;
    this.item = item;
    this.recipe = recipe;
    this.producer = producer;
    this.deliverTo = deliverTo;
    this.isUserRequest = isUserRequest;
    this.remainingSteps = recipe.getLatency();
  }

  /**
   * Gets the order number of this request.
   *@return the order number of this request.
   */
  public int getOrderNum() {
    return orderNum;
  }

  /**
   * Gets the requested item.
   *@return the requested item.
   */
  public Item getItem() {
    return item;
  }
  /**
   * Gets the recipe for the requested item.
   *@return the recipe for the requested item.
   */
  public Recipe getRecipe() {
    return recipe;
  }
  /**
   * Gets the producer building to produce the requested item.
   *@return the producer building.
   */
  public Building getProducer() {
    return producer;
  }

  /**
   * Gets the target delivery building for the requested item.
   * 
   * @return the target delivery building if the request is issued from another
   *         building, null if the request is made by user.
   */
  public Building getDeliverTo() {
    if (isUserRequest) {
      return null;
    } else {
      return deliverTo;
    }
  }
  /**
   * Tells if this request is made by user.
   * @return true if the request is made by user, false if the request is issued by another building wanting the item.
   */
  public boolean isUserRequest() {
    return isUserRequest;
  }

  /**
   * Process the request by one step. Returns whether the request if completed.
   *
   * @return true if the request is completed, false otherwise.
   */
  public boolean process() {
    // Return true if request is already finished processing
    if (isCompleted()) {
        return true;
    }

    // Consume one step, then return the request status
    remainingSteps--;
    return isCompleted();
  }

  /**
   * Whether the request has been processed completely.
   *
   * @return true if the request is completed, false otherwise.
   */
  public boolean isCompleted() {
    return remainingSteps <= 0;
  }
}
