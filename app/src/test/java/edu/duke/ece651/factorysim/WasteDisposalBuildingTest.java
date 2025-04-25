package edu.duke.ece651.factorysim;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;

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
  public void test_toJson_includesStorage() {
    wasteDisposal.addToStorage(sawdust, 120);
    wasteDisposal.addToStorage(plasticWaste, 70);
    JsonObject json = wasteDisposal.toJson();

    assertTrue(json.has("storage"));
    JsonObject storage = json.getAsJsonObject("storage");
    assertNotNull(storage);
    assertEquals(120, storage.get("sawdust").getAsInt());
    assertEquals(70, storage.get("plastic_waste").getAsInt());
  }

  @Test
  public void test_toJson_withNullLocation() {
    wasteDisposal.setLocation(null);
    JsonObject json = wasteDisposal.toJson();
    assertFalse(json.has("x"));
    assertFalse(json.has("y"));
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
  public void test_markForRemoval() throws NoSuchFieldException, IllegalAccessException {
    assertFalse(wasteDisposal.isPendingRemoval());
    assertTrue(wasteDisposal.markForRemoval());

    // reset the private field directly (no try/catch)
    java.lang.reflect.Field pendingRemovalField = Building.class.getDeclaredField("pendingRemoval");
    pendingRemovalField.setAccessible(true);
    pendingRemovalField.set(wasteDisposal, false);

    // simulate a pending request so markForRemoval() fails
    Recipe wasteRecipe = TestUtils.makeTestRecipe("sawdust", 0, 1);
    Building sourceBuilding = new TestUtils.MockBuilding("source");
    sourceBuilding.setLocation(new Coordinate(5, 5));
    Request request = new Request(1, sawdust, wasteRecipe, sourceBuilding, wasteDisposal);
    wasteDisposal.prependPendingRequest(request);

    assertFalse(wasteDisposal.markForRemoval());
    assertTrue(wasteDisposal.isPendingRemoval());
  }

  @Test
  public void test_processWasteType_inProgress() {
    wasteDisposal.addToStorage(sawdust, 100);
    wasteDisposal.step();
    wasteDisposal.step();
    assertEquals(50, wasteDisposal.getStorageNumberOf(sawdust));
  }

  @Test
  public void test_processWasteType_cycleComplete() {
    wasteDisposal.addToStorage(plasticWaste, 90); // enough for 3 cycles
    wasteDisposal.step(); // step 1 - process starts
    wasteDisposal.step(); // step 2 - in progress
    wasteDisposal.step(); // step 3 - should complete

    // After 3rd step, one batch should be disposed
    assertTrue(wasteDisposal.getStorageNumberOf(plasticWaste) <= 60); // disposed 30
  }

  @Test
  public void test_processWasteType_noWasteAvailable() {
    // No sawdust added
    wasteDisposal.step(); // Should not crash or start any processing
    assertTrue(wasteDisposal.isFinished());
  }

  @Test
  public void test_deliverTo_notifiesSimulation() {
    // Create a testable simulation that we can verify method calls on
    TestableSimulation testSimulation = new TestableSimulation(simulation.getWorld(), 0, new StreamLogger(System.out));
    
    // Create a source building that will deliver waste
    TestUtils.MockBuilding sourceBuilding = new TestUtils.MockBuilding("source_building");
    // Set the simulation field in the source building
    java.lang.reflect.Field simulationField;
    try {
      simulationField = Building.class.getDeclaredField("simulation");
      simulationField.setAccessible(true);
      simulationField.set(sourceBuilding, testSimulation);
    } catch (Exception e) {
      fail("Failed to set simulation field: " + e.getMessage());
    }
    sourceBuilding.setLocation(new Coordinate(5, 5));
    
    // Create our waste disposal building with the testable simulation
    LinkedHashMap<Item, Integer> capacityMap = new LinkedHashMap<>();
    capacityMap.put(sawdust, 400);
    LinkedHashMap<Item, Integer> rateMap = new LinkedHashMap<>();
    rateMap.put(sawdust, 50);
    LinkedHashMap<Item, Integer> timeStepsMap = new LinkedHashMap<>();
    timeStepsMap.put(sawdust, 2);
    
    WasteDisposalBuilding testWasteDisposal = new WasteDisposalBuilding(
        "test_waste_disposal", capacityMap, rateMap, timeStepsMap, testSimulation);
    testWasteDisposal.setLocation(new Coordinate(10, 10));
    
    // Deliver some waste
    int quantity = 30;
    sourceBuilding.deliverTo(testWasteDisposal, sawdust, quantity, false);
    
    // Verify the waste was added to storage
    assertEquals(quantity, testWasteDisposal.getStorageNumberOf(sawdust));
    
    // Verify the simulation was notified about the waste delivery
    assertTrue(testSimulation.wasteDeliveryMethodCalled);
    assertEquals(sawdust, testSimulation.wasteDeliveredItem);
    assertEquals(quantity, testSimulation.wasteDeliveredQuantity);
    assertEquals(testWasteDisposal, testSimulation.wasteDeliveredDestination);
    assertEquals(sourceBuilding, testSimulation.wasteDeliveredSource);
  }
  
  // Helper class to verify simulation method calls
  private static class TestableSimulation extends Simulation {
    public boolean wasteDeliveryMethodCalled = false;
    public Item wasteDeliveredItem = null;
    public int wasteDeliveredQuantity = 0;
    public Building wasteDeliveredDestination = null;
    public Building wasteDeliveredSource = null;
    
    public TestableSimulation(World world, int delay, Logger logger) {
      super(world, delay, logger);
    }
    
    @Override
    public void onWasteDelivered(Item item, int quantity, Building destination, Building source) {
      super.onWasteDelivered(item, quantity, destination, source);
      wasteDeliveryMethodCalled = true;
      wasteDeliveredItem = item;
      wasteDeliveredQuantity = quantity;
      wasteDeliveredDestination = destination;
      wasteDeliveredSource = source;
    }
  }

  @Test
  public void test_findWasteDisposalBuilding() {
    // Create a test world and simulation
    World world = WorldBuilder.buildEmptyWorld(100, 100);
    TestUtils.MockSimulation simulation = new TestUtils.MockSimulation();
    simulation.setWorld(world);
    simulation.setLogger(new StreamLogger(System.out));
    
    // Create waste types
    Item sawdust = new Item("sawdust");
    Item plasticWaste = new Item("plastic_waste");
    Item chemicalWaste = new Item("chemical_waste");
    
    // Create a factory building that will generate waste
    Recipe factoryRecipe = TestUtils.makeTestRecipe("widget", 0, 1);
    Type factoryType = new Type("WidgetFactory", List.of(factoryRecipe));
    FactoryBuilding factory = new FactoryBuilding(factoryType, "widget_factory", new ArrayList<>(), simulation);
    factory.setLocation(new Coordinate(1, 1));
    
    // Create first waste disposal building with low capacity for sawdust only
    LinkedHashMap<Item, Integer> capacityMap1 = new LinkedHashMap<>();
    capacityMap1.put(sawdust, 10);
    LinkedHashMap<Item, Integer> rateMap1 = new LinkedHashMap<>();
    rateMap1.put(sawdust, 5);
    LinkedHashMap<Item, Integer> timeStepsMap1 = new LinkedHashMap<>();
    timeStepsMap1.put(sawdust, 2);
    WasteDisposalBuilding wasteDisposal1 = new WasteDisposalBuilding("waste_disposal_1", capacityMap1, rateMap1, timeStepsMap1, simulation);
    wasteDisposal1.setLocation(new Coordinate(10, 10));
    
    // Create second waste disposal building with higher capacity for sawdust and plastic
    LinkedHashMap<Item, Integer> capacityMap2 = new LinkedHashMap<>();
    capacityMap2.put(sawdust, 100);
    capacityMap2.put(plasticWaste, 50);
    LinkedHashMap<Item, Integer> rateMap2 = new LinkedHashMap<>();
    rateMap2.put(sawdust, 20);
    rateMap2.put(plasticWaste, 10);
    LinkedHashMap<Item, Integer> timeStepsMap2 = new LinkedHashMap<>();
    timeStepsMap2.put(sawdust, 3);
    timeStepsMap2.put(plasticWaste, 4);
    WasteDisposalBuilding wasteDisposal2 = new WasteDisposalBuilding("waste_disposal_2", capacityMap2, rateMap2, timeStepsMap2, simulation);
    wasteDisposal2.setLocation(new Coordinate(20, 20));
    
    // Add all buildings to the world
    List<Building> buildings = new ArrayList<>();
    buildings.add(factory);
    buildings.add(wasteDisposal1);
    buildings.add(wasteDisposal2);
    world.setBuildings(buildings);
    
    // Create a recipe with waste byproducts
    Item widget = new Item("widget");
    Item wood = new Item("wood");
    HashMap<Item, Integer> ingredients = new HashMap<>();
    ingredients.put(wood, 2);
    HashMap<Item, Integer> wasteByProducts = new HashMap<>();
    wasteByProducts.put(sawdust, 15); // More than wasteDisposal1 can handle
    Recipe widgetRecipe = new Recipe(widget, ingredients, 5, wasteByProducts);
    
    // Add recipes to the world
    world.setRecipes(List.of(widgetRecipe));
    
    // TEST 1: Using reflection to access the private findWasteDisposalBuilding method directly
    try {
      java.lang.reflect.Method findWasteDisposalBuildingMethod = 
          Building.class.getDeclaredMethod("findWasteDisposalBuilding", Item.class, int.class);
      findWasteDisposalBuildingMethod.setAccessible(true);
      
      // Test with sawdust (15 units)
      WasteDisposalBuilding result1 = 
          (WasteDisposalBuilding) findWasteDisposalBuildingMethod.invoke(factory, sawdust, 15);
      // Expect wasteDisposal2 since wasteDisposal1 can only handle 10 units
      assertEquals(wasteDisposal2, result1);
      
      // Test with sawdust (10 units)
      WasteDisposalBuilding result2 = 
          (WasteDisposalBuilding) findWasteDisposalBuildingMethod.invoke(factory, sawdust, 10);
      // Either disposal building could handle this, but it should choose wasteDisposal1 (checking first)
      assertEquals(wasteDisposal1, result2);
      
      // Test with plastic waste
      WasteDisposalBuilding result3 = 
          (WasteDisposalBuilding) findWasteDisposalBuildingMethod.invoke(factory, plasticWaste, 40);
      // Only wasteDisposal2 can handle plastic waste
      assertEquals(wasteDisposal2, result3);
      
      // Test with chemical waste (no building can handle)
      WasteDisposalBuilding result4 = 
          (WasteDisposalBuilding) findWasteDisposalBuildingMethod.invoke(factory, chemicalWaste, 10);
      // Should return null since no waste disposal building can handle chemical waste
      assertNull(result4);
      
    } catch (Exception e) {
      fail("Failed to test findWasteDisposalBuilding method: " + e.getMessage());
    }
  }
}
