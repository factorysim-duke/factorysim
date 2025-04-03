package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements Shortest Job First (SJF) request policy.
 */
public class SjfRequestPolicy extends RequestPolicy {
  @Override
  public Request selectRequest(Building producer, List<Request> requests) {
    if (requests.isEmpty()) {
      return null;
    }
    Request shortestRequest = requests.getFirst();
    for (Request request : requests) {
      if (request.getRecipe().getLatency() < shortestRequest.getRecipe().getLatency()) {
        shortestRequest = request;
      }
    }
    return shortestRequest;
  }

  @Override
  public String getName() {
    return "sjf";
  }
}
