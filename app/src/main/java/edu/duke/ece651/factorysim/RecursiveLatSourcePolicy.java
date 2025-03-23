package edu.duke.ece651.factorysim;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Implements recursive latency (recursivelat) source policy.
 */
public class RecursiveLatSourcePolicy extends SourcePolicy {
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
    // build here
    // TODO: please make sure to call `onReportScore.accept(source, score)` here so verbosity 2 can log scores
    return bestSource;
  }

  @Override
  public String getName() {
    return "recursivelat";
  }
}
