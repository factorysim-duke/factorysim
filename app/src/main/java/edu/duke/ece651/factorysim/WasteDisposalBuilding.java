package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents a waste disposal building in the simulation.
 */
public class WasteDisposalBuilding extends Building {
  private final LinkedHashMap<Item, Integer> maxCapacityMap;
  // disposal rate (quantity per time step)
  private final LinkedHashMap<Item, Integer> disposalRateMap;
  // total time steps needed for disposal
  private final LinkedHashMap<Item, Integer> totalDisposalTimeStepsNeededMap;
  // current disposal progress (time steps remaining for items)
  private final LinkedHashMap<Item, Integer> currentDisposalProgressMap;
  // quantity that is currently being processed
  private final LinkedHashMap<Item, Integer> processingWasteMap;
  // quantity reserved for incoming waste
  private final LinkedHashMap<Item, Integer> reservedCapacityMap;

  /**
   * Constructs a waste disposal building.
   *
   * @param name             is the name of the building.
   * @param wasteTypes       is a map of waste types to their capacities.
   * @param disposalRateMaps is a map of waste types to their disposal rates.
   * @param timeSteps        is a map of waste types to their disposal time steps.
   * @param simulation       is the simulation this building is in.
   */
  public WasteDisposalBuilding(String name, LinkedHashMap<Item, Integer> wasteTypes,
      LinkedHashMap<Item, Integer> disposalRateMaps, LinkedHashMap<Item, Integer> timeSteps, Simulation simulation) {
    super(name, new ArrayList<>(), simulation);
    this.maxCapacityMap = new LinkedHashMap<>(wasteTypes);
    this.disposalRateMap = new LinkedHashMap<>(disposalRateMaps);
    this.totalDisposalTimeStepsNeededMap = new LinkedHashMap<>(timeSteps);
    this.currentDisposalProgressMap = new LinkedHashMap<>();
    this.processingWasteMap = new LinkedHashMap<>();
    this.reservedCapacityMap = new LinkedHashMap<>();

    for (Item wasteType : maxCapacityMap.keySet()) {
      currentDisposalProgressMap.put(wasteType, 0);
      processingWasteMap.put(wasteType, 0);
      reservedCapacityMap.put(wasteType, 0);
    }
  }

  /**
   * Checks if this waste disposal can process a given waste item.
   * 
   * @param item is the waste item to be checked.
   * @return true if this waste disposal can process this waste, false otherwise.
   */
  @Override
  public boolean canProduce(Item item) {
    return maxCapacityMap.containsKey(item);
  }

  /**
   * Checks if the waste disposal has capacity for a specific waste type.
   *
   * @param wasteType is the waste type item to check.
   * @param quantity  is the quantity to check for.
   * @return true if there is capacity, false otherwise.
   */
  public boolean hasCapacityFor(Item wasteType, int quantity) {
    if (!maxCapacityMap.containsKey(wasteType)) {
      return false;
    }
    int currentStorage = getStorageNumberOf(wasteType);
    if (currentStorage == -1)
      currentStorage = 0;
    int reserved = reservedCapacityMap.getOrDefault(wasteType, 0);
    int maxCapacityMapNum = maxCapacityMap.get(wasteType);
    return (currentStorage + reserved + quantity) <= maxCapacityMapNum;
  }

  /**
   * Reserves capacity for incoming waste to prevent overflows.
   *
   * @param wasteType is the waste type item to reserve capacity for.
   * @param quantity  is the quantity to reserve.
   * @return true if the reservation succeeded, false otherwise.
   */
  public boolean reserveCapacity(Item wasteType, int quantity) {
    if (!hasCapacityFor(wasteType, quantity)) {
      return false;
    }
    int currentReserved = reservedCapacityMap.getOrDefault(wasteType, 0);
    reservedCapacityMap.put(wasteType, currentReserved + quantity);
    return true;
  }

  /**
   * Releases reserved capacity when waste is received.
   *
   * @param wasteType is the waste type item to release reservation for.
   * @param quantity  is the quantity to release.
   */
  public void releaseReservedCapacity(Item wasteType, int quantity) {
    int currentReserved = reservedCapacityMap.getOrDefault(wasteType, 0);
    reservedCapacityMap.put(wasteType, Math.max(0, currentReserved - quantity));
  }

  /**
   * Steps the waste disposal building forward in time.
   * Processes waste items according to their disposal rates and time steps.
   */
  @Override
  public void step() {
    super.step();

    // process waste disposal
    for (Item wasteType : maxCapacityMap.keySet()) {
      processWasteType(wasteType);
    }
  }

  /**
   * Process a specific waste type item.
   *
   * @param wasteType is the waste type item to process.
   */
  private void processWasteType(Item wasteType) {
    int currentQuantity = getStorageNumberOf(wasteType);
    if (currentQuantity <= 0) {
      return;
    }

    // if not currently processing this waste item, start a new batch
    if (processingWasteMap.get(wasteType) == 0) {
      int rate = disposalRateMap.get(wasteType);
      int quantityToProcess = Math.min(currentQuantity, rate);
      if (quantityToProcess > 0) {
        processingWasteMap.put(wasteType, quantityToProcess);
        currentDisposalProgressMap.put(wasteType, totalDisposalTimeStepsNeededMap.get(wasteType));
        // remove waste from storage into processing
        takeFromStorage(wasteType, quantityToProcess);
      }
    }

    // if already processing, decrease the time step counter
    int progress = currentDisposalProgressMap.get(wasteType);
    if (progress > 0 && processingWasteMap.get(wasteType) > 0) {
      progress--;
      currentDisposalProgressMap.put(wasteType, progress);

      // if the disposal is complete, reset processing
      if (progress == 0) {
        processingWasteMap.put(wasteType, 0);
      }
    }
  }

  /**
   * Gets the waste types this building can dispose of.
   *
   * @return a list of waste type items.
   */
  public List<Item> getWasteTypes() {
    return new ArrayList<>(maxCapacityMap.keySet());
  }

  /**
   * Gets the maximum capacity for a waste type item.
   *
   * @param wasteType is the waste type item to check.
   * @return the maximum capacity for that waste type item, or -1 if not
   *         supported.
   */
  public int getMaxCapacityFor(Item wasteType) {
    return maxCapacityMap.getOrDefault(wasteType, -1);
  }

  /**
   * Gets the disposal rate for a waste type item.
   *
   * @param wasteType is the waste type item to check.
   * @return the disposal rate for that waste type item, or -1 if not supported.
   */
  public int getDisposalRateFor(Item wasteType) {
    return disposalRateMap.getOrDefault(wasteType, -1);
  }

  /**
   * Gets the disposal time steps for a waste type item.
   *
   * @param wasteType is the waste type item to check.
   * @return the disposal time steps for that waste type item, or -1 if not
   *         supported.
   */
  public int getDisposalTimeStepsFor(Item wasteType) {
    return totalDisposalTimeStepsNeededMap.getOrDefault(wasteType, -1);
  }

  /**
   * Converts the waste disposal building to a JSON object.
   *
   * @return a JsonObject representing the waste disposal building.
   */
  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getName());
    json.addProperty("type", "waste_disposal");

    // waste
    JsonObject wasteTypesJson = new JsonObject();
    for (Map.Entry<Item, Integer> entry : maxCapacityMap.entrySet()) {
      JsonObject wasteDetails = new JsonObject();
      Item wasteType = entry.getKey();
      wasteDetails.addProperty("capacity", maxCapacityMap.get(wasteType));
      wasteDetails.addProperty("rate", disposalRateMap.get(wasteType));
      wasteDetails.addProperty("timeSteps", totalDisposalTimeStepsNeededMap.get(wasteType));
      wasteTypesJson.add(wasteType.getName(), wasteDetails);
    }
    json.add("waste_types", wasteTypesJson);

    // storage
    JsonObject storage = new JsonObject();
    if (!getStorage().isEmpty()) {
      for (Map.Entry<Item, Integer> entry : getStorage().entrySet()) {
        storage.addProperty(entry.getKey().getName(), entry.getValue());
      }
    }
    json.add("storage", storage);

    // location
    if (this.getLocation() != null) {
      json.addProperty("x", this.getLocation().getX());
      json.addProperty("y", this.getLocation().getY());
    }

    return json;
  }
}
