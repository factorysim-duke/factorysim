package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Defines a policy for selecting a request to pop.
 */
public interface RequestPolicy {
  /**
   * Pops a request from the given list based on the policy.<br/>
   * Note that the method should pop and return the request by modifying the <code>requests</code> list.
   *
   * @param producer the building that handles the request.
   * @param requests the request list reference for modification.
   * @return the popped request based on the policy.
   */
  Request popRequest(Building producer, List<Request> requests);
}
