package edu.duke.ece651.factorysim;

/**
 * Represents an interface in the simulation.
 */
public interface Policy {
  /**
   * Returns the name of the policy. Such as fifo, ready, qlen, etc.
   *
   * @return the name of the policy.
   */
  public String getName();

  /**
   * Gets the policy type's name.
   * 
   * @return the policy type's name.
   */
  public String getPolicyTypeName();
}
