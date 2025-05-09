package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;

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
  private int remainingSteps;
  private String status;

  /**
   * Constructs a request.
   *
   * @param orderNum  is the order number.
   * @param item      is the item being requested.
   * @param recipe    is the recipe for the requested item.
   * @param producer  is the producer building for the requested item (and
   *                  recipe).
   * @param deliverTo is the target building to receive the requested item. If
   *                  it's null, this is a user request.
   */
  public Request(int orderNum, Item item, Recipe recipe, Building producer, Building deliverTo) {
    this.orderNum = orderNum;
    this.item = item;
    this.recipe = recipe;
    this.producer = producer;
    this.deliverTo = deliverTo;
    this.remainingSteps = recipe.getLatency();
    status = "pending";
  }

  /**
   * Gets the order number of this request.
   *
   * @return the order number of this request.
   */
  public int getOrderNum() {
    return orderNum;
  }

  /**
   * Gets the requested item.
   *
   * @return the requested item.
   */
  public Item getItem() {
    return item;
  }

  /**
   * Gets the recipe for the requested item.
   *
   * @return the recipe for the requested item.
   */
  public Recipe getRecipe() {
    return recipe;
  }

  /**
   * Gets the producer building to produce the requested item.
   *
   * @return the producer building.
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
    if (isUserRequest()) {
      return null;
    } else {
      return deliverTo;
    }
  }

  /**
   * Tells if this request is made by user.
   *
   * @return true if the request is made by user, false if the request is issued
   *         by another building wanting the item.
   */
  public boolean isUserRequest() {
    return deliverTo == null;
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

  /**
   * Gets the remaining steps of the request.
   *
   * @return the remaining steps of finishing the request.
   */
  public int getRemainingSteps() {
    return remainingSteps;
  }

  /**
   * Retrieves the current status.
   *
   * @return the current status as a String.
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status to the specified value.
   *
   * @param status the new status to set.
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Sets the remainingSteps to the specified value.
   *
   * @param remainingSteps the new status to set.
   */
  public void setRemainingSteps(int remainingSteps) {
    this.remainingSteps = remainingSteps;
  }

  /**
   * Converts the Request object to a JSON representation.
   *
   * @return a JsonObject representing the request.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();

    json.addProperty("orderNum", orderNum);
    json.addProperty("item", item.getName());

    json.addProperty("recipe", recipe.getOutput().getName());
    json.addProperty("producer", producer.getName());
    if (deliverTo != null) {
      json.addProperty("deliverTo", deliverTo.getName());
    } else {
      json.addProperty("deliverTo", "null");
    }
    json.addProperty("remainingSteps", remainingSteps);
    json.addProperty("status", status);
    return json;
  }

}
