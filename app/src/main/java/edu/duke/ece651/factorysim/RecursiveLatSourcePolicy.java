package edu.duke.ece651.factorysim;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Implements recursive latency (recursivelat) source policy.
 */
public class RecursiveLatSourcePolicy extends SourcePolicy {
  /**
   * Represents the usage of a factory building.
   */
  public static class Usage {
    private final Map<Item, Map<List<BuildingId>, Integer>> storageUsed = new HashMap<>();
    private final Set<RequestInUse> inProgressUsed = new HashSet<>();

    public void addStorageUsed(Item item, List<BuildingId> path, int amount) {
      storageUsed
          .computeIfAbsent(item, k -> new HashMap<>())
          .merge(path, amount, Integer::sum);
    }

    public void addInProgressUsed(RequestInUse requestInUse) {
      inProgressUsed.add(requestInUse);
    }

    public boolean isInProgressUsed(RequestInUse requestInUse) {
      return inProgressUsed.contains(requestInUse);
    }

    public int getStorageUsed(Item item, List<BuildingId> path) {
      return storageUsed
          .getOrDefault(item, Collections.emptyMap())
          .getOrDefault(path, 0);
    }

    public void clearReservations(List<BuildingId> pathPrefix) {
      // remove any storageUsed entries whose key "starts with" pathPrefix
      storageUsed.values().forEach(map -> 
          map.keySet().removeIf(path -> pathStartsWith(path, pathPrefix)));
      // remove any inProgressUsed whose path "starts with" pathPrefix
      inProgressUsed.removeIf(req -> pathStartsWith(req.getPath(), pathPrefix));
    }

    private boolean pathStartsWith(List<BuildingId> path, List<BuildingId> prefix) {
      if (path.size() < prefix.size()) {
        return false;
      }
      for (int i = 0; i < prefix.size(); i++) {
        if (!path.get(i).equals(prefix.get(i))) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * A unique identifier for a building.
   */
  public static class BuildingId {
    private final Building building;
    private final String uniqueId;

    public BuildingId(Building building, String uniqueId) {
      this.building = building;
      this.uniqueId = uniqueId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      BuildingId that = (BuildingId) o;
      return building.equals(that.building) && uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(building, uniqueId);
    }
  }

  /**
   * A request that is currently in use.
   */
  public static class RequestInUse {
    private final Building factory;
    private final Request request;
    private final List<BuildingId> path;

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
      return factory.equals(that.factory)
          && request.equals(that.request)
          && path.equals(that.path);
    }

    @Override
    public int hashCode() {
      return Objects.hash(factory, request, path);
    }
  }

  /**
   * Estimates the time it takes to complete a request.
   * 
   * @param request the request to estimate
   * @param factory the factory building
   * @param usage   usage/reservations info
   * @param path    path to this request
   * @return        the estimated time
   */
  private int estimate(Request request, Building factory, Usage usage, List<BuildingId> path) {
    Request currentRequest = factory.getCurrentRequest();
    // Always return the request's remaining steps if it is *the* in-progress request
    if (currentRequest != null && currentRequest.equals(request)) {
      return request.getRemainingSteps();
    }

    // If not the exact current request in progress, we compute fresh.
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
        // Gather estimates from all sources that can make this ingredient
        for (Building source : factory.getAvailableSourcesForItem(ingredient)) {
          List<BuildingId> newPath = new ArrayList<>(path);
          newPath.add(new BuildingId(source, uid));

          Recipe subRecipe = source.getRecipeForItem(ingredient);
          Request subReq = new Request(
              factory.getSimulation().getOrderNum(), 
              ingredient, 
              subRecipe, 
              source, 
              factory
          );
          int est = estimate(subReq, source, usage, newPath);
          estimates.add(Map.entry(source, est));
        }

        // Sort by ascending time
        estimates.sort(Map.Entry.comparingByValue());
        int parallel = Math.min(needed, estimates.size());
        if (parallel > 0) {
          // We can make 'parallel' copies of the ingredient in parallel
          // so we add the max time among them
          int maxOfUsed = estimates.subList(0, parallel)
                                   .stream()
                                   .mapToInt(Map.Entry::getValue)
                                   .max()
                                   .getAsInt();
          time += maxOfUsed;

          // Clear out reservations for any *unused* source
          for (int i = parallel; i < estimates.size(); i++) {
            List<BuildingId> toClear = new ArrayList<>(path);
            toClear.add(new BuildingId(estimates.get(i).getKey(), uid));
            usage.clearReservations(toClear);
          }
          needed -= parallel;
        } else {
          // parallel == 0 => cannot produce the needed ingredient => break
          break;
        }
      }
    }
    return time;
  }

  /**
   * Calculates the total estimated time of the building's current queue.
   */
  private int calculateTotalEstimate(Building source) {
    Usage usage = new Usage();
    int total = 0;
    String uniqueId = UUID.randomUUID().toString();
    List<BuildingId> path = new ArrayList<>();
    path.add(new BuildingId(source, uniqueId));

    for (Request req : source.getPendingRequests()) {
      total += estimate(req, source, usage, path);
    }
    return total;
  }

  /**
   * Selects a source according to recursivelat.
   */
  @Override
  public Building selectSource(Item item,
                               List<Building> sources,
                               BiConsumer<Building, Integer> onReportScore) {
    if (sources.isEmpty()) {
      return null;
    }
    Building best = sources.get(0);
    int bestScore = calculateTotalEstimate(best);
    onReportScore.accept(best, bestScore);

    for (int i = 1; i < sources.size(); i++) {
      Building s = sources.get(i);
      int score = calculateTotalEstimate(s);
      onReportScore.accept(s, score);
      if (score < bestScore) {
        best = s;
        bestScore = score;
      }
    }
    return best;
  }

  @Override
  public String getName() {
    return "recursivelat";
  }
}
