package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements Queue Length (qlen) source policy.
 */
public class QLenSourcePolicy implements SourcePolicy {
  /**
   * Selects a source to produce item according to the policy.
   * 
   * @param item    is the requested item.
   * @param sources is the list of buildings that can produce the item.
   * @return the source building according to the policy.
   */
  @Override
  public Building selectSource(Item item, List<Building> sources) {
    if (sources.isEmpty()) {
      return null;
    }
    Building bestSource = sources.get(0);
    int minRequestNum = bestSource.getNumOfPendingRequests();
    for (Building source : sources) {
      if (source.getNumOfPendingRequests() < minRequestNum) {
        minRequestNum = source.getNumOfPendingRequests();
        bestSource = source;
      }
    }
    return bestSource;
  }
}
