package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Defines a policy for selecting a source in the simulation.
 */
public interface SourcePolicy {
  /**
   * Selects a source to produce item according to the policy.
   * 
   * @param item    is the requested item.
   * @param sources is the list of buildings that can produce the item.
   * @return the source building according to the policy.
   */
  Building selectSource(Item item, List<Building> sources);
}
