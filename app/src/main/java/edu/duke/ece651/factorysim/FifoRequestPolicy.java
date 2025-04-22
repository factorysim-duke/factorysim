package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements FIFO (First In, First Out) request policy.
 */
public class FifoRequestPolicy extends RequestPolicy {
  @Override
  public Request selectRequest(Building producer, List<Request> requests) {
    if (requests.isEmpty()) {
      System.out.println("[DEBUG] FifoRequestPolicy: " + producer.getName() + " has no requests to select");
      return null;
    }
    Request selected = requests.getFirst();
    System.out.println("[DEBUG] FifoRequestPolicy: " + producer.getName() 
                     + " selected first request for " + selected.getItem().getName()
                     + ", deliverTo=" + (selected.getDeliverTo() != null ? selected.getDeliverTo().getName() : "user"));
    return selected;
  }

  @Override
  public String getName() {
    return "fifo";
  }
}
