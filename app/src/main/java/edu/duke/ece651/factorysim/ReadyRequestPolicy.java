package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Request policy that selects the first request that can be done with the current
 * storage.
 */
public class ReadyRequestPolicy extends RequestPolicy {
  @Override
  public Request selectRequest(Building producer, List<Request> requests) {
    Iterator<Request> iterator = requests.iterator();
    while (iterator.hasNext()) {
      Request request = iterator.next();
      if (producer.hasAllIngredientsFor(request.getRecipe())) {
        return request;
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return "ready";
  }
}
