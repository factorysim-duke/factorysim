package edu.duke.ece651.factorysim;

/**
 * Represents a factory to create source policies.
 */
public class SourcePolicyFactory {
  /**
   * Creates a source policy based on the policy name.
   * 
   * @param policyName the name of the policy to create.
   * @return the created source policy.
   */
  public static SourcePolicy createPolicy(String policyName) {
    switch (policyName.toLowerCase()) {
      case "qlen":
        return new QLenSourcePolicy();
      case "simplelat":
        // return new SimpleLatPolicy();
      case "recursivelat":
        // return new RecursiveLatPolicy();
      default:
        throw new IllegalArgumentException("Unknown source policy: " + policyName);
    }
  }
}
