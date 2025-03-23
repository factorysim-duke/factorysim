package edu.duke.ece651.factorysim;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Implements simple latency (simplelat) source policy.
 */
public class SimpleLatSourcePolicy extends SourcePolicy {
  /**
   * Selects a source to produce item according to the policy.
   * 
   * @param item    is the requested item.
   * @param sources is the list of buildings that can produce the item.
   * @param onReportScore is the callback function to report score.
   * @return the source building according to the policy.
   */
  @Override
  public Building selectSource(Item item,
                               List<Building> sources,
                               BiConsumer<Building, Integer> onReportScore) {
    if (sources.isEmpty()) {
      return null;
    }
    Building bestSource = sources.get(0);
    int minRemainingLatencies = bestSource.sumRemainingLatencies();
    for (Building source : sources) {
      int newNum = source.sumRemainingLatencies();
      onReportScore.accept(source, newNum); // Report source score
      if (newNum < minRemainingLatencies) {
        minRemainingLatencies = newNum;
        bestSource = source;
      }
    }
    return bestSource;
  }

  @Override
  public String getName() {
    return "simplelat";
  }
}
