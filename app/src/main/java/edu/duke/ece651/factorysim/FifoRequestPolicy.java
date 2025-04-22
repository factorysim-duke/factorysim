package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements FIFO (First In, First Out) request policy.
 */
public class FifoRequestPolicy extends RequestPolicy {
  @Override
  public Request selectRequest(Building producer, List<Request> requests) {
    if (requests.isEmpty()) {
      return null;
    }
    Request first = requests.get(0);
    return first;
  }

  @Override
  public String getName() {
    return "fifo";
  }
}
