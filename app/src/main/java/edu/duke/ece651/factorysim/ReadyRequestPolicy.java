package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Request policy that pops the first request that can be done with the current
 * storage.
 */
public class ReadyRequestPolicy extends RequestPolicy {
  @Override
  public Request popRequest(Building producer, List<Request> requests) {
    Iterator<Request> iterator = requests.iterator();
    while (iterator.hasNext()) {
      Request request = iterator.next();
      if (producer.canProduce(request.getItem())) {
        iterator.remove();
        return request;
      }
    }
    return null;
  }
}
