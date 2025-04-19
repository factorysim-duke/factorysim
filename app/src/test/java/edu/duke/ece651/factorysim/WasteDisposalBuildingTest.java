package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WasteDisposalBuildingTest {
  private Simulation simulation;
  private WasteDisposalBuilding wasteDisposal;
  private Item sawdust;
  private Item plasticWaste;

  @BeforeEach
  public void setUp() {
    World world = WorldBuilder.buildEmptyWorld(100, 100);
    simulation = new Simulation(world, 0, new StreamLogger(System.out));
    sawdust = new Item("sawdust");
    plasticWaste = new Item("plastic_waste");
    LinkedHashMap<Item, Integer> capacityMap = new LinkedHashMap<>();
    capacityMap.put(sawdust, 400);
    capacityMap.put(plasticWaste, 200);
    LinkedHashMap<Item, Integer> rateMap = new LinkedHashMap<>();
    rateMap.put(sawdust, 50);
    rateMap.put(plasticWaste, 30);
    LinkedHashMap<Item, Integer> timeStepsMap = new LinkedHashMap<>();
    timeStepsMap.put(sawdust, 2); // consume 50 sawdusts all at once every 2 steps
    timeStepsMap.put(plasticWaste, 3); // consume 30 plasticWaste all at once every 3 steps

    wasteDisposal = new WasteDisposalBuilding("test_waste_disposal", capacityMap, rateMap, timeStepsMap, simulation);
    wasteDisposal.setLocation(new Coordinate(10, 10));

    List<Building> buildings = new ArrayList<>();
    buildings.add(wasteDisposal);
    world.setBuildings(buildings);
  }

  @Test
  public void test_constructor() {
    assertEquals("test_waste_disposal", wasteDisposal.getName());
    assertEquals(new Coordinate(10, 10), wasteDisposal.getLocation());
    List<Item> wasteTypes = wasteDisposal.getWasteTypes();
    assertEquals(2, wasteTypes.size());
    assertTrue(wasteTypes.contains(sawdust));
    assertTrue(wasteTypes.contains(plasticWaste));

    // capacity
    assertEquals(400, wasteDisposal.getMaxCapacityFor(sawdust));
    assertEquals(200, wasteDisposal.getMaxCapacityFor(plasticWaste));
    assertEquals(-1, wasteDisposal.getMaxCapacityFor(new Item("unknown_waste")));
    // disposal rates
    assertEquals(50, wasteDisposal.getDisposalRateFor(sawdust));
    assertEquals(30, wasteDisposal.getDisposalRateFor(plasticWaste));
    assertEquals(-1, wasteDisposal.getDisposalRateFor(new Item("unknown_waste")));
    // time steps
    assertEquals(2, wasteDisposal.getDisposalTimeStepsFor(sawdust));
    assertEquals(3, wasteDisposal.getDisposalTimeStepsFor(plasticWaste));
    assertEquals(-1, wasteDisposal.getDisposalTimeStepsFor(new Item("unknown_waste")));
  }

  @Test
  public void test_can_produce() {
    assertTrue(wasteDisposal.canProduce(sawdust));
    assertTrue(wasteDisposal.canProduce(plasticWaste));
    assertFalse(wasteDisposal.canProduce(new Item("unknown_waste")));
  }

  @Test
  public void test_has_capacity_for() {
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 400));
    assertTrue(wasteDisposal.hasCapacityFor(plasticWaste, 200));
    assertFalse(wasteDisposal.hasCapacityFor(sawdust, 401));
    assertFalse(wasteDisposal.hasCapacityFor(plasticWaste, 201));
    wasteDisposal.addToStorage(sawdust, 100);
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 300));
    assertFalse(wasteDisposal.hasCapacityFor(sawdust, 301));
    assertFalse(wasteDisposal.hasCapacityFor(new Item("unknown_waste"), 10));
  }

  @Test
  public void test_reserve_and_release_capacity() {
    // reserving capacity
    assertTrue(wasteDisposal.reserveCapacity(sawdust, 200));
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 200));
    assertFalse(wasteDisposal.hasCapacityFor(sawdust, 201));
    // releasing reserved capacity
    wasteDisposal.releaseReservedCapacity(sawdust, 50);
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 250));
    // reserving beyond capacity
    assertFalse(wasteDisposal.reserveCapacity(sawdust, 251));
    // release all and check we are back to full capacity
    wasteDisposal.releaseReservedCapacity(sawdust, 150);
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 400));
    // releasing more than reserved
    wasteDisposal.reserveCapacity(sawdust, 100);
    wasteDisposal.releaseReservedCapacity(sawdust, 200);
    assertTrue(wasteDisposal.hasCapacityFor(sawdust, 400));
  }

  @Test
  public void test_to_json() {
    JsonObject json = wasteDisposal.toJson();
    assertEquals("test_waste_disposal", json.get("name").getAsString());
    assertEquals("waste_disposal", json.get("type").getAsString());
    JsonObject wasteTypesJson = json.getAsJsonObject("waste_types");
    assertTrue(wasteTypesJson.has("sawdust"));
    assertTrue(wasteTypesJson.has("plastic_waste"));
    JsonObject sawdustJson = wasteTypesJson.getAsJsonObject("sawdust");
    assertEquals(400, sawdustJson.get("capacity").getAsInt());
    assertEquals(50, sawdustJson.get("rate").getAsInt());
    assertEquals(2, sawdustJson.get("timeSteps").getAsInt());
    assertEquals(10, json.get("x").getAsInt());
    assertEquals(10, json.get("y").getAsInt());
  }

  @Test
  public void test_building_with_waste_byproducts_from_json() {
    Simulation jsonSimulation = new Simulation("src/main/resources/electronics_with_waste.json", 0,
        new StreamLogger(System.out));
    World world = jsonSimulation.getWorld();
    Building originalFurnitureFactory = world.getBuildingFromName("furniture_factory");
    WasteDisposalBuilding woodWasteDisposal = (WasteDisposalBuilding) world.getBuildingFromName("wood_waste_disposal");
    Item wood = new Item("wood");
    Item chair = new Item("chair");
    Item sawdust = new Item("sawdust");
    Recipe chairRecipe = world.getRecipeForItem(chair);

    assertTrue(chairRecipe.hasWasteByProducts());
    assertTrue(chairRecipe.getWasteByProducts().containsKey(sawdust));
    assertEquals(20, chairRecipe.getWasteByProducts().get(sawdust));

    Type factoryType = ((FactoryBuilding) originalFurnitureFactory).getFactoryType();
    FactoryBuilding furnitureFactory = new FactoryBuilding(factoryType, "test_furniture_factory",
        originalFurnitureFactory.getSources(), jsonSimulation);
    furnitureFactory.setLocation(originalFurnitureFactory.getLocation());
    List<Building> buildings = new ArrayList<>(world.getBuildings());
    buildings.add(furnitureFactory);
    world.setBuildings(buildings);
    furnitureFactory.addToStorage(wood, 2);
    Request chairRequest = new Request(1, chair, chairRecipe, furnitureFactory, null);
    furnitureFactory.setCurrentRequest(chairRequest);
    chairRequest.setRemainingSteps(0);

    assertEquals(-1, woodWasteDisposal.getStorageNumberOf(sawdust));
    furnitureFactory.finishCurrentRequest();
    // assertEquals(20, woodWasteDisposal.getStorageNumberOf(sawdust));
    assertEquals(1, furnitureFactory.getStorageNumberOf(chair));
    assertNull(furnitureFactory.getCurrentRequest());
  }

  @Test
  public void test_is_finished_with_waste_processing() {
    assertTrue(wasteDisposal.isFinished());
    Item sawdust = new Item("sawdust");
    wasteDisposal.addToStorage(sawdust, 50);
    assertFalse(wasteDisposal.isFinished());
    Item plastic = new Item("plastic_waste");
    wasteDisposal.reserveCapacity(plastic, 10);
    assertFalse(wasteDisposal.isFinished());
    wasteDisposal.takeFromStorage(sawdust, 50);
    assertFalse(wasteDisposal.isFinished());
    wasteDisposal.releaseReservedCapacity(plastic, 10);
    assertTrue(wasteDisposal.isFinished());
  }

  @Test
  public void test_finish_delivery_releases_waste_capacity() {
    World world = new World();
    world.setTileMapDimensions(10, 10);
    world.setBuildings(new java.util.ArrayList<>());
    Simulation sim = new Simulation(world, 0, new StreamLogger(System.out));
    LinkedHashMap<Item, Integer> wasteTypes = new LinkedHashMap<>();
    LinkedHashMap<Item, Integer> disposalRates = new LinkedHashMap<>();
    LinkedHashMap<Item, Integer> timeSteps = new LinkedHashMap<>();
    Item sawdust = new Item("sawdust");
    wasteTypes.put(sawdust, 100);
    disposalRates.put(sawdust, 10);
    timeSteps.put(sawdust, 2);
    WasteDisposalBuilding wasteDisposal = new WasteDisposalBuilding("waste_disposal", wasteTypes, disposalRates,
        timeSteps, sim);
    wasteDisposal.setLocation(new Coordinate(1, 1));
    world.tryAddBuilding(wasteDisposal);
    Building sourceBuilding = new TestUtils.MockBuilding("source");
    sourceBuilding.setLocation(new Coordinate(2, 2));
    boolean reserved = wasteDisposal.reserveCapacity(sawdust, 20);
    assertTrue(reserved);
    Delivery delivery = new Delivery(sourceBuilding, wasteDisposal, sawdust, 20, 5);

    while (!delivery.isArrive()) {
      delivery.step();
    }
    delivery.finishDelivery();
    assertEquals(20, wasteDisposal.getStorageNumberOf(sawdust));
    assertFalse(wasteDisposal.isFinished());
    wasteDisposal.step();
    wasteDisposal.step();
    wasteDisposal.step();
    wasteDisposal.step();
    assertTrue(wasteDisposal.isFinished());
    assertEquals(-1, wasteDisposal.getStorageNumberOf(sawdust));
  }
  
  @Test
  public void test_canBeRemovedImmediately() {
    assertTrue(wasteDisposal.canBeRemovedImmediately());
    Recipe wasteRecipe = TestUtils.makeTestRecipe("sawdust", 0, 1);
    Building sourceBuilding = new TestUtils.MockBuilding("source");
    sourceBuilding.setLocation(new Coordinate(5, 5));
    Request request = new Request(1, sawdust, wasteRecipe, sourceBuilding, wasteDisposal);
    wasteDisposal.prependPendingRequest(request);
    assertFalse(wasteDisposal.canBeRemovedImmediately());
    wasteDisposal.getPendingRequests().clear();
    assertTrue(wasteDisposal.canBeRemovedImmediately());
    wasteDisposal.setCurrentRequest(request);
    assertFalse(wasteDisposal.canBeRemovedImmediately());
    wasteDisposal.setCurrentRequest(null);
    assertTrue(wasteDisposal.canBeRemovedImmediately());
    wasteDisposal.addToStorage(sawdust, 10);
    assertTrue(wasteDisposal.canBeRemovedImmediately());
  }
  
  @Test
  public void test_canAcceptRequest() {
    Recipe wasteRecipe = TestUtils.makeTestRecipe("sawdust", 0, 1);
    Building sourceBuilding = new TestUtils.MockBuilding("source");
    sourceBuilding.setLocation(new Coordinate(5, 5));
    Request request = new Request(1, sawdust, wasteRecipe, sourceBuilding, wasteDisposal);
    assertTrue(wasteDisposal.canAcceptRequest(request));
    wasteDisposal.prependPendingRequest(request);
    assertFalse(wasteDisposal.markForRemoval());
    assertTrue(wasteDisposal.isPendingRemoval());
    assertFalse(wasteDisposal.canAcceptRequest(request));
  }
  
  @Test
  public void test_markForRemoval() {
    assertFalse(wasteDisposal.isPendingRemoval());
    assertTrue(wasteDisposal.markForRemoval());
    java.lang.reflect.Field pendingRemovalField;
    try {
      pendingRemovalField = Building.class.getDeclaredField("pendingRemoval");
      pendingRemovalField.setAccessible(true);
      pendingRemovalField.set(wasteDisposal, false);
    } catch (Exception e) {
      fail("Failed to reset pendingRemoval field: " + e.getMessage());
    }
    Recipe wasteRecipe = TestUtils.makeTestRecipe("sawdust", 0, 1);
    Building sourceBuilding = new TestUtils.MockBuilding("source");
    sourceBuilding.setLocation(new Coordinate(5, 5));
    Request request = new Request(1, sawdust, wasteRecipe, sourceBuilding, wasteDisposal);
    wasteDisposal.prependPendingRequest(request);
    assertFalse(wasteDisposal.markForRemoval());
    assertTrue(wasteDisposal.isPendingRemoval());
  }
}
