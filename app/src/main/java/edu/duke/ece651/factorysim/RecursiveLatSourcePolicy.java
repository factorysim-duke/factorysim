package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Implements recursive latency (recursivelat) source policy.
 */
public class RecursiveLatSourcePolicy extends SourcePolicy {
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
    // build here
    return bestSource;
  }
}
