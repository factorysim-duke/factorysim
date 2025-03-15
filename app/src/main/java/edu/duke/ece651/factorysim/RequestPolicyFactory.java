package edu.duke.ece651.factorysim;

/**
 * Factory class for creating request policies.
 */
public class RequestPolicyFactory {
  /**
   * Creates a request policy based on the policy name.
   * 
   * @param policyName the name of the policy to create.
   * @return the created request policy.
   */
  public static RequestPolicy createPolicy(String policyName) {
    switch (policyName.toLowerCase()) {
      case "fifo":
        return new FifoRequestPolicy();
      case "ready":
        return new ReadyRequestPolicy();
    //   case "sjf":
    //     return new ShortestJobFirstPolicy();
      default:
        throw new IllegalArgumentException("Unknown request policy: " + policyName);
    }
  }
}