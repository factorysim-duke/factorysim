package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Defines a policy for selecting a source in the simulation.
 */
public abstract class SourcePolicy implements Policy {
  /**
   * Selects a source to produce item according to the policy.
   * 
   * @param item    is the requested item.
   * @param sources is the list of buildings that can produce the item.
   * @return the source building according to the policy.
   */
  public abstract Building selectSource(Item item, List<Building> sources);

  
  /**
   * Gets the policy type's name.
   * 
   * @return the policy type's name.
   */
  @Override
  public String getPolicyTypeName() {
    return "source";
  }
}
