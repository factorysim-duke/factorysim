package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements FIFO (First In, First Out) request policy.
 */
public class FifoRequestPolicy implements RequestPolicy {
  @Override
  public Request popRequest(Building producer, List<Request> requests) {
    if (requests.isEmpty()) {
      return null;
    }
    return requests.remove(0);
  }
}