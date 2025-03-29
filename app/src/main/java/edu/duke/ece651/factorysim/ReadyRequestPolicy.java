package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Request policy that selects the oldest request that can be done with the current
 * storage. A request is ready whenever the building's storage has the required ingredients
 * for that recipe. Note that selecting an older ready request may make newer ready
 * requests unready by consuming shared ingredients.
 */
public class ReadyRequestPolicy extends RequestPolicy {
  @Override
  public Request selectRequest(Building producer, List<Request> requests) {
    // Find the first (oldest) ready request
    for (Request request : requests) {
      if (producer.hasAllIngredientsFor(request.getRecipe())) {
        return request;
      }
    }
    
    // If no ready requests, return null
    return null;
  }

  @Override
  public String getName() {
    return "ready";
  }
}
