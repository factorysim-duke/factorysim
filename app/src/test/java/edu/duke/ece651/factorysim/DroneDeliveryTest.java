package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DroneDeliveryTest {
  private Simulation simulation;
  private DronePortBuilding dronePortBuilding;
  private FactoryBuilding factoryBuilding1;
  private FactoryBuilding factoryBuilding2;
  private WasteDisposalBuilding wasteDisposalBuilding;
  private TestLogger logger;
  private Drone drone;
  private DronePort dronePort;
  private Item item;

  @BeforeEach
  void setUp() {
    logger = new TestLogger();
    World world = new World();
    simulation = new Simulation(world, 2, logger);
    dronePortBuilding = new DronePortBuilding("Drone Port", new ArrayList<Building>(), simulation);
    dronePortBuilding.setLocation(new Coordinate(50, 50));
    Type factoryType = new Type("Factory", new ArrayList<Recipe>());

    factoryBuilding1 = new FactoryBuilding(factoryType, "Factory 1", new ArrayList<Building>(), simulation);
    factoryBuilding1.setLocation(new Coordinate(60, 50)); // within drone port radius
    factoryBuilding2 = new FactoryBuilding(factoryType, "Factory 2", new ArrayList<Building>(), simulation);
    factoryBuilding2.setLocation(new Coordinate(50, 60)); // within drone port radius
    
    // Create waste disposal building with empty maps
    LinkedHashMap<Item, Integer> wasteTypes = new LinkedHashMap<>();
    LinkedHashMap<Item, Integer> disposalRates = new LinkedHashMap<>();
    LinkedHashMap<Item, Integer> timeSteps = new LinkedHashMap<>();
    item = new Item("TestItem");
    wasteTypes.put(item, 10);
    disposalRates.put(item, 1);
    timeSteps.put(item, 1);
    wasteDisposalBuilding = new WasteDisposalBuilding("Waste Disposal", wasteTypes, disposalRates, timeSteps, simulation);
    wasteDisposalBuilding.setLocation(new Coordinate(40, 50)); // within drone port radius

    List<Building> buildings = new ArrayList<>();
    buildings.add(dronePortBuilding);
    buildings.add(factoryBuilding1);
    buildings.add(factoryBuilding2);
    buildings.add(wasteDisposalBuilding);
    world.setBuildings(buildings);

    factoryBuilding1.addToStorage(item, 10);
    
    // Access the drone port from the building
    dronePort = dronePortBuilding.getDronePort();
    
    // Create a drone
    drone = new Drone();
  }

  @Test
  void test_createDrone() {
    assertTrue(dronePortBuilding.createDrone());
    assertEquals(1, dronePortBuilding.getDroneCount());
    assertTrue(logger.getLastMessage().contains("[drone created]"));
  }

  @Test
  void test_maxDrones() {
    for (int i = 0; i < 10; i++) {
      assertTrue(dronePortBuilding.createDrone());
    }
    assertFalse(dronePortBuilding.createDrone());
    assertEquals(10, dronePortBuilding.getDroneCount());
  }

  @Test
  void test_droneDelivery() {
    dronePortBuilding.createDrone();
    simulation.addDelivery(factoryBuilding1, factoryBuilding2, item, 5);
    assertTrue(logger.getLastMessage().contains("[drone delivery scheduled]"));
    assertEquals(0, dronePortBuilding.getDroneCount());
    
    // drone should arrive at source
    simulation.step(1);
    assertEquals(10, factoryBuilding1.getStorageNumberOf(item)); // 10 items left in source after pickup
    assertEquals(-1, factoryBuilding2.getStorageNumberOf(item)); // none delivered yet

    // drone should arrive at destination, hand the resource, and return to port
    simulation.step(100);
    assertEquals(5, factoryBuilding2.getStorageNumberOf(item)); // 5 items at destination
    assertEquals(1, dronePortBuilding.getDroneCount()); // drone returned to port
  }

  @Test
  void test_dronePriority() {
    dronePortBuilding.createDrone();
    Path path = new Path();
    path.emplaceBack(factoryBuilding1.getLocation(), false, 0);
    path.emplaceBack(factoryBuilding2.getLocation(), false, 0);
    simulation.getPathList().add(path);
    simulation.addDelivery(factoryBuilding1, factoryBuilding2, item, 5);
    assertTrue(logger.getLastMessage().contains("[drone delivery scheduled]"));
    assertEquals(0, dronePortBuilding.getDroneCount()); // drone is in use
  }

  @Test
  void test_deliveryOutsideRadius() {
    dronePortBuilding.createDrone();
    Type factoryType = new Type("Factory", new ArrayList<Recipe>());
    FactoryBuilding farFactory = new FactoryBuilding(factoryType, "Far Factory", new ArrayList<Building>(), simulation);
    farFactory.setLocation(new Coordinate(100, 100)); // outside drone radius
    simulation.getWorld().getBuildings().add(farFactory);
    factoryBuilding1.addToStorage(item, 5);
    Path path = new Path();
    path.emplaceBack(factoryBuilding1.getLocation(), false, 0);
    path.emplaceBack(farFactory.getLocation(), false, 0);
    simulation.getPathList().add(path);
    simulation.addDelivery(factoryBuilding1, farFactory, item, 5);

    assertEquals(1, dronePortBuilding.getDroneCount());
  }
  
  @Test
  void test_deliveryToWasteDisposal() {
    // Create a direct DroneDelivery to the waste disposal building
    dronePortBuilding.createDrone();
    
    // Reserve capacity in waste disposal
    int quantity = 5;
    assertTrue(wasteDisposalBuilding.reserveCapacity(item, quantity));
    
    // Verify the waste disposal can handle this waste type
    assertTrue(wasteDisposalBuilding.hasCapacityFor(item, quantity));
    assertEquals(10, wasteDisposalBuilding.getMaxCapacityFor(item));
    
    // Create DroneDelivery directly to avoid any issues with simulation addDelivery
    Drone testDrone = new Drone();
    DroneDelivery delivery = new DroneDelivery(dronePort, testDrone, 
                                   factoryBuilding1, wasteDisposalBuilding, 
                                   item, quantity);
    
    // Initially, the drone is heading to source
    assertEquals(DroneDelivery.DeliveryState.TO_SOURCE, delivery.getState());
    
    // Step until we reach the source
    while (delivery.getState() == DroneDelivery.DeliveryState.TO_SOURCE) {
      delivery.step();
    }
    
    // Now the drone is heading to destination
    assertEquals(DroneDelivery.DeliveryState.TO_DESTINATION, delivery.getState());
    
    // Step until we reach the destination (wasteDisposalBuilding)
    while (delivery.getState() == DroneDelivery.DeliveryState.TO_DESTINATION) {
      delivery.step();
    }
    
    // Now the drone should be returning to drone port
    assertEquals(DroneDelivery.DeliveryState.RETURNING, delivery.getState());
    
    // At this point, the waste should have been delivered
    // Check if the waste was delivered and possibly processed
    int storageAmount = wasteDisposalBuilding.getStorageNumberOf(item);

    assertEquals(quantity, storageAmount);
    
    // Step until the drone returns to port
    while (!delivery.isArrive()) {
      delivery.step();
    }
    
    // Verify the drone has completed its journey
    assertTrue(delivery.isArrive());
  }
  
  @Test
  void test_droneDeliveryGetters() {
    // Create delivery directly
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // Test getters
    assertEquals(dronePort, delivery.getDronePort());
    assertEquals(drone, delivery.getDrone());
    assertEquals(DroneDelivery.DeliveryState.TO_SOURCE, delivery.getState());
    assertEquals(factoryBuilding1.getLocation(), delivery.getTargetCoordinate());
  }
  
  @Test
  void test_isArrive() {
    // Create delivery directly
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // Should not be arrived initially
    assertFalse(delivery.isArrive());
    
    // Simulate getting to source
    while (delivery.getState() == DroneDelivery.DeliveryState.TO_SOURCE) {
      delivery.step();
    }
    assertFalse(delivery.isArrive());
    
    // Simulate getting to destination
    while (delivery.getState() == DroneDelivery.DeliveryState.TO_DESTINATION) {
      delivery.step();
    }
    assertFalse(delivery.isArrive());
    
    // Simulate returning to drone port
    while (!delivery.isArrive()) {
      delivery.step();
    }
    assertTrue(delivery.isArrive());
  }
  
  @Test
  void test_toJson() {
    // Create delivery directly
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // Test JSON conversion
    JsonObject json = delivery.toJson();
    assertEquals("DroneDelivery", json.get("type").getAsString());
    assertEquals(dronePort.getBuilding().getName(), json.get("dronePort").getAsString());
    assertEquals(DroneDelivery.DeliveryState.TO_SOURCE.name(), json.get("state").getAsString());
    assertEquals(factoryBuilding1.getName(), json.get("source").getAsString());
    assertEquals(factoryBuilding2.getName(), json.get("destination").getAsString());
  }
  
  @Test
  void test_updateCurrentCoordinate() {
    // Create delivery directly
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // Save the original coordinate
    Coordinate originalCoord = delivery.getCurrentCoordinate();
    
    // Call method that should do nothing
    delivery.updateCurrentCoordinate(new ArrayList<>());
    
    // Coordinate should not change
    assertEquals(originalCoord, delivery.getCurrentCoordinate());
  }
  
  @Test
  void test_finishDelivery() {
    // Create delivery directly
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // This method should do nothing, just call it for coverage
    delivery.finishDelivery();
    
    // State should remain unchanged
    assertEquals(DroneDelivery.DeliveryState.TO_SOURCE, delivery.getState());
  }
  
  @Test
  void test_calculateDeliveryTime() {
    // Create coordinates with varying distances
    Coordinate c1 = new Coordinate(0, 0);
    Coordinate c2 = new Coordinate(10, 0); // dx = 10, dy = 0, distance = 10
    Coordinate c3 = new Coordinate(0, 10); // dx = 0, dy = 10, distance = 10
    Coordinate c4 = new Coordinate(5, 5);  // dx = 5, dy = 5, distance = 10
    
    // Create a test delivery to access the calculateDeliveryTime method
    DroneDelivery delivery = new DroneDelivery(dronePort, drone, 
                                                factoryBuilding1, factoryBuilding2, 
                                                item, 3);
    
    // Test each step of the drone delivery lifecycle
    delivery.step(); // Go toward source
    
    while(delivery.getState() == DroneDelivery.DeliveryState.TO_SOURCE && delivery.deliveryTime > 0) {
      delivery.step();
    }
    
    // Now at source, going to destination
    assertEquals(DroneDelivery.DeliveryState.TO_DESTINATION, delivery.getState());
    assertEquals(factoryBuilding2.getLocation(), delivery.getTargetCoordinate());
    
    while(delivery.getState() == DroneDelivery.DeliveryState.TO_DESTINATION && delivery.deliveryTime > 0) {
      delivery.step();
    }
    
    // Now at destination, going back to drone port
    assertEquals(DroneDelivery.DeliveryState.RETURNING, delivery.getState());
    assertEquals(dronePortBuilding.getLocation(), delivery.getTargetCoordinate());
  }
  
  @Test
  void test_verbosityLevels() {
    // Test with verbosity 0
    World world = new World();
    // Create a new logger for this test to avoid interference from other tests
    TestLogger testLogger = new TestLogger();
    Simulation simNoVerbose = new Simulation(world, 0, testLogger);
    DronePortBuilding dpb = new DronePortBuilding("Silent Port", new ArrayList<Building>(), simNoVerbose);
    dpb.setLocation(new Coordinate(1, 1));
    world.setBuildings(List.of(dpb, factoryBuilding1, factoryBuilding2));
    
    // Create a drone at the silent port
    DronePort silentPort = dpb.getDronePort();
    Drone silentDrone = new Drone();
    
    // Create delivery with verbosity 0
    int initialMessageCount = testLogger.getMessageCount();
    DroneDelivery silentDelivery = new DroneDelivery(silentPort, silentDrone, 
                                               factoryBuilding1, factoryBuilding2, 
                                               item, 3);
    
    // No log message should be generated for verbosity 0
    assertEquals(initialMessageCount, testLogger.getMessageCount());
    
    // Complete the delivery
    while (!silentDelivery.isArrive()) {
      silentDelivery.step();
    }
    
    // Message count should still be the same for verbosity 0
    assertEquals(initialMessageCount, testLogger.getMessageCount(),
                "No messages should be logged when verbosity is 0");
  }

  private static class TestLogger implements Logger {
    private final List<String> messages = new ArrayList<>();
    @Override
    public void log(String message) {
      messages.add(message);
    }
    public String getLastMessage() {
      return messages.get(messages.size() - 1);
    }
    public int getMessageCount() {
      return messages.size();
    }
  }
}
