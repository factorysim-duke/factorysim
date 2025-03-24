package edu.duke.ece651.factorysim;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Implements recursive latency (recursivelat) source policy.
 */
public class RecursiveLatSourcePolicy extends SourcePolicy {
  private static class Usage {
    private Map<Item, Map<List<BuildingId>, Integer>> storageUsed = new HashMap<>();
    private Set<RequestInUse> inProgressUsed = new HashSet<>();

    public void addStorageUsed(Item item, List<BuildingId> path, int amount) {
      if (!storageUsed.containsKey(item)) {
        storageUsed.put(item, new HashMap<>());
      }
      storageUsed.get(item).merge(path, amount, Integer::sum);
    }

    public void addInProgressUsed(RequestInUse requestInUse) {
      inProgressUsed.add(requestInUse);
    }

    public boolean isInProgressUsed(RequestInUse requestInUse) {
      return (inProgressUsed.contains(requestInUse));
    }

    public int getStorageUsed(Item item, List<BuildingId> path) {
      return storageUsed.getOrDefault(item, Collections.emptyMap()).getOrDefault(path, 0);
    }

    public void clearReservations(List<BuildingId> pathPrefix) {
      storageUsed.values().forEach(map -> map.keySet().removeIf(path -> pathStartsWith(path, pathPrefix)));
      inProgressUsed.removeIf(req -> pathStartsWith(req.getPath(), pathPrefix));
    }

    private boolean pathStartsWith(List<BuildingId> path, List<BuildingId> prefix) {
      if (path.size() < prefix.size()) return false;
      for (int i = 0; i < prefix.size(); i++) {
        if (!path.get(i).equals(prefix.get(i))) return false;
      }
      return true;
    }
  }

  private static class BuildingId {
    private Building building;
    private String uniqueId;

    public BuildingId(Building building, String uniqueId) {
      this.building = building;
      this.uniqueId = uniqueId;
    }

    /*
     * Rewrite the equals method to compare the building and uniqueId
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      BuildingId buildingId = (BuildingId) o;
      return building.equals(buildingId.building) && uniqueId.equals(buildingId.uniqueId);
    }

    /*
     * Rewrite the hashCode method to hash the building and uniqueId
     */
    @Override
    public int hashCode() {
      return Objects.hash(building, uniqueId);
    }
  }

  private class RequestInUse {
    private Building factory;
    private Request request;
    private List<BuildingId> path;

    public RequestInUse(Building factory, Request request, List<BuildingId> path) {
      this.factory = factory;
      this.request = request;
      this.path = path;
    }

    public List<BuildingId> getPath() {
      return path;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RequestInUse that = (RequestInUse) o;
      return factory.equals(that.factory) && request.equals(that.request) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
      return Objects.hash(factory, request, path);
    }
  }

  private int estimate(Request request, Building factory, Usage usage, List<BuildingId> path) {
    Request currentRequest = factory.getCurrentRequest();
    if (currentRequest != null && currentRequest.equals(request)) {
      RequestInUse requestInUse = new RequestInUse(factory, request, path);
      if (!usage.isInProgressUsed(requestInUse)) {
        usage.addInProgressUsed(requestInUse);
        return request.getRemainingSteps();
      }
    }

    int time = request.getRemainingSteps();
    LinkedHashMap<Item, Integer> ingredients = request.getRecipe().getIngredients();
    for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
      Item ingredient = entry.getKey();
      int needed = entry.getValue();

      int available = factory.getStorageNumberOf(ingredient);
      int used = usage.getStorageUsed(ingredient, path);
      int usable = Math.min(available - used, needed);
      if (usable > 0) {
        usage.addStorageUsed(ingredient, path, usable);
        needed -= usable;
      }
      while (needed > 0) {
        String uid = UUID.randomUUID().toString();
        List<Map.Entry<Building, Integer>> estimates = new ArrayList<>();
        for (Building source : factory.getAvailableSourcesForItem(ingredient)) {
          List<BuildingId> newPath = new ArrayList<>(path);
          newPath.add(new BuildingId(source, uid));
          Recipe recipe = source.getRecipeForItem(ingredient);
          Request subReq = new Request(factory.getSimulation().getOrderNum(), ingredient, recipe, source, factory);
          int est = estimate(subReq, source, usage, newPath);
          estimates.add(Map.entry(source, est));
        }

        estimates.sort(Map.Entry.comparingByValue());
        int parallel = Math.min(needed, estimates.size());
        if (parallel > 0) {
          time += estimates.subList(0, parallel).stream().mapToInt(Map.Entry::getValue).max().getAsInt();
          for (int i = parallel; i < estimates.size(); i++) {
            List<BuildingId> toClear = new ArrayList<>(path);
            toClear.add(new BuildingId(estimates.get(i).getKey(), uid));
            usage.clearReservations(toClear);
          }
          needed -= parallel;
        } else break;
      }
    }

    return time;
  }

  /**
   * Calculates the total estimate time of the source building.
   * 
   * @param source is the source building.
   * @return the total estimate.
   */
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

  /**
   * Gets the name of the source policy (recursivelat).
   * 
   * @return the name of the source policy.
   */
  @Override
  public String getName() {
    return "recursivelat";
  }
}
