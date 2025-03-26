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

    /**
     * Adds storage used for a given item and path.
     * 
     * @param item   the item to add storage used for.
     * @param path   the path to add storage used for.
     * @param amount the amount of storage used.
     */
    public void addStorageUsed(Item item, List<BuildingId> path, int amount) {
      if (!storageUsed.containsKey(item)) {
        storageUsed.put(item, new HashMap<>());
      }
      if (!storageUsed.get(item).containsKey(path)) {
        storageUsed.get(item).put(path, 0);
      }
      storageUsed.get(item).put(path, storageUsed.get(item).get(path) + amount);
    }

    /**
     * Adds a request to the in progress used list.
     * 
     * @param requestInUse the request to add.
     */
    public void addInProgressUsed(RequestInUse requestInUse) {
      inProgressUsed.add(requestInUse);
    }

    /**
     * Checks if a request is in progress.
     * 
     * @param requestInUse the request to check.
     * @return true if the request is in progress, false otherwise.
     */
    public boolean isInProgressUsed(RequestInUse requestInUse) {
      return inProgressUsed.contains(requestInUse);
    }

    /**
     * Gets the storage used for a given item and path.
     * 
     * @param item the item to get storage used for.
     * @param path the path to get storage used for.
     * @return the storage used for the given item and path.
     */
    public int getStorageUsed(Item item, List<BuildingId> path) {
      return storageUsed
          .getOrDefault(item, Collections.emptyMap())
          .getOrDefault(path, 0);
    }

    /**
     * Clears reservations for a given path prefix.
     * 
     * @param pathPrefix the prefix to clear reservations for.
     */
    public void clearReservations(List<BuildingId> pathPrefix) {
      for (Map.Entry<Item, Map<List<BuildingId>, Integer>> entry : storageUsed.entrySet()) {
        entry.getValue().keySet().removeIf(path -> pathStartsWith(path, pathPrefix));
      }
      inProgressUsed.removeIf(req -> pathStartsWith(req.getPath(), pathPrefix));
    }

    /**
     * Checks if a path starts with a given prefix.
     * 
     * @param path   the path to check.
     * @param prefix the prefix to check against.
     * @return true if the path starts with the prefix, false otherwise.
     */
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

    /**
     * Constructs a BuildingId object.
     * 
     * @param building the building.
     * @param uniqueId the unique identifier.
     */
    public BuildingId(Building building, String uniqueId) {
      this.building = building;
      this.uniqueId = uniqueId;
    }

    /**
     * Checks if two BuildingId objects are equal.
     * 
     * @param o the object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
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

    /**
     * Returns the hash code of the BuildingId object.
     * 
     * @return the hash code of the BuildingId object.
     */
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

    /**
     * Constructs a RequestInUse object.
     * 
     * @param factory the factory building.
     * @param request the request.
     * @param path    the path.
     */
    public RequestInUse(Building factory, Request request, List<BuildingId> path) {
      this.factory = factory;
      this.request = request;
      this.path = path;
    }

    /**
     * Gets the path of the request.
     * 
     * @return the path of the request.
     */
    public List<BuildingId> getPath() {
      return path;
    }

    /**
     * Checks if two RequestInUse objects are equal.
     * 
     * @param o the object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      RequestInUse that = (RequestInUse) o;
      return factory.equals(that.factory)
          && request.equals(that.request)
          && path.equals(that.path);
    }

    /**
     * Returns the hash code of the RequestInUse object.
     * 
     * @return the hash code of the RequestInUse object.
     */
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
   * @return the estimated time
   */
  private int estimate(Request request, Building factory, Usage usage, List<BuildingId> path) {
    Request currentRequest = factory.getCurrentRequest();
    if (currentRequest != null && currentRequest.equals(request)) {
      return request.getRemainingSteps();
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

          Recipe subRecipe = source.getRecipeForItem(ingredient);
          Request subReq = new Request(
              factory.getSimulation().getOrderNum(),
              ingredient,
              subRecipe,
              source,
              factory);
          int est = estimate(subReq, source, usage, newPath);
          estimates.add(Map.entry(source, est));
        }
        estimates.sort(Map.Entry.comparingByValue());
        int parallel = Math.min(needed, estimates.size());
        if (parallel > 0) {
          int maxOfUsed = 0;
          for (int i = 0; i < parallel; i++) {
            maxOfUsed = Math.max(maxOfUsed, estimates.get(i).getValue());
          }
          time += maxOfUsed;

          for (int i = parallel; i < estimates.size(); i++) {
            List<BuildingId> toClear = new ArrayList<>(path);
            toClear.add(new BuildingId(estimates.get(i).getKey(), uid));
            usage.clearReservations(toClear);
          }
          needed -= parallel;
        } else {
          break;
        }
      }
    }
    return time;
  }

  /**
   * Calculates the total estimated time of the building's current queue.
   * 
   * @param source the building to calculate the total estimate for.
   * @return the total estimated time.
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
   * 
   * @param item          the item to select a source for.
   * @param sources       the list of sources to select from.
   * @param onReportScore a callback to report the score of the selected source.
   * @return the selected source.
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

  /**
   * Returns the name of the source policy.
   * 
   * @return the name of the source policy.
   */
  @Override
  public String getName() {
    return "recursivelat";
  }
}
