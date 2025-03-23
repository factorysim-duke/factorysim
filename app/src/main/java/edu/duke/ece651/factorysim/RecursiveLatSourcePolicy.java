package edu.duke.ece651.factorysim;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Implements recursive latency (recursivelat) source policy.
 */
public class RecursiveLatSourcePolicy extends SourcePolicy {
  private static class Usage {
    private Map<String, Map<List<BuildingId>, Integer>> storageUsed = new HashMap<>();
  }

  private static class BuildingId {
    private Building building;
    private String uniqueId;

    public BuildingId(Building building, String uniqueId) {
      this.building = building;
      this.uniqueId = uniqueId;
    }
  }

  private int estimate(Request request, Building factory, Usage usage, List<BuildingId> path) {
    return 0; // TODO: Implement this
  }

  private int calculateTotalEstimate(Building source) {
    Usage usage = new Usage();
    int total = 0;
    String uniqueId = UUID.randomUUID().toString();
    List<BuildingId> path = new ArrayList<>();
    path.add(new BuildingId(source, uniqueId));
    for (Request request : source.getPendingRequests()) {
      total += estimate(request, source, usage, path);
    }
    return total;
  }

  /**
   * Selects a source to produce item according to the policy.
   * 
   * @param item          is the requested item.
   * @param sources       is the list of buildings that can produce the item.
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
    int minScore = calculateTotalEstimate(bestSource);
    onReportScore.accept(bestSource, minScore);
    for (int i = 1; i < sources.size(); i++) {
      Building source = sources.get(i);
      int score = calculateTotalEstimate(source);
      onReportScore.accept(source, score);
      if (score < minScore) {
        minScore = score;
        bestSource = source;
      }
    }
    return bestSource;
  }

  @Override
  public String getName() {
    return "recursivelat";
  }
}
