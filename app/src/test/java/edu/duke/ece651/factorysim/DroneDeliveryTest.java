package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DroneDeliveryTest {
  private Simulation simulation;
  private DronePortBuilding dronePortBuilding;
  private FactoryBuilding factoryBuilding1;
  private FactoryBuilding factoryBuilding2;
  private TestLogger logger;

  @BeforeEach
  void setUp() {
    logger = new TestLogger();
    World world = new World();
    simulation = new Simulation(world, 2, logger);
    dronePortBuilding = new DronePortBuilding("Drone Port", new ArrayList<>(), simulation);
    dronePortBuilding.setLocation(new Coordinate(50, 50));
    Type factoryType = new Type("Factory", new ArrayList<>());

    factoryBuilding1 = new FactoryBuilding(factoryType, "Factory 1", new ArrayList<>(), simulation);
    factoryBuilding1.setLocation(new Coordinate(60, 50)); // within drone port radius
    factoryBuilding2 = new FactoryBuilding(factoryType, "Factory 2", new ArrayList<>(), simulation);
    factoryBuilding2.setLocation(new Coordinate(50, 60)); // within drone port radius

    List<Building> buildings = new ArrayList<>();
    buildings.add(dronePortBuilding);
    buildings.add(factoryBuilding1);
    buildings.add(factoryBuilding2);
    world.setBuildings(buildings);

    Item item = new Item("TestItem");
    factoryBuilding1.addToStorage(item, 10);
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
    Item item = new Item("TestItem");
    simulation.addDelivery(factoryBuilding1, factoryBuilding2, item, 5);
    assertTrue(logger.getLastMessage().contains("[drone delivery scheduled]"));
    assertEquals(0, dronePortBuilding.getDroneCount());
    
    // drone should arrive at source
    simulation.step(1);
    assertEquals(10, factoryBuilding1.getStorageNumberOf(item)); // 10 items left in source after pickup
    assertEquals(-1, factoryBuilding2.getStorageNumberOf(item)); // none delivered yet

    // Commented because DroneDelivery is changed to not take from source storage
//    // drone should pick items and depart
//    simulation.step(1);
//    assertEquals(5, factoryBuilding1.getStorageNumberOf(item)); // 5 items left in source
//    assertEquals(-1, factoryBuilding2.getStorageNumberOf(item)); // none delivered yet

    // drone should arrive at destination, hand the resource, and return to port
    simulation.step(100);
    assertEquals(5, factoryBuilding2.getStorageNumberOf(item)); // 5 items at destination
    assertEquals(1, dronePortBuilding.getDroneCount()); // drone returned to port
  }

  @Test
  void test_dronePriority() {
    dronePortBuilding.createDrone();
    Item item = new Item("TestItem");
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
    Type factoryType = new Type("Factory", new ArrayList<>());
    FactoryBuilding farFactory = new FactoryBuilding(factoryType, "Far Factory", new ArrayList<>(), simulation);
    farFactory.setLocation(new Coordinate(100, 100)); // outside drone radius
    simulation.getWorld().getBuildings().add(farFactory);
    Item item = new Item("TestItem");
    factoryBuilding1.addToStorage(item, 5);
    Path path = new Path();
    path.emplaceBack(factoryBuilding1.getLocation(), false, 0);
    path.emplaceBack(farFactory.getLocation(), false, 0);
    simulation.getPathList().add(path);
    simulation.addDelivery(factoryBuilding1, farFactory, item, 5);

    assertEquals(1, dronePortBuilding.getDroneCount());
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
  }
}
