package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DronePortTest {
  private World world;
  private Simulation simulation;
  private DronePortBuilding dronePortBuilding;
  private DronePort dronePort;

  @BeforeEach
  void setUp() {
    world = new World();
    simulation = new Simulation(world, 0, new TestLogger());
    dronePortBuilding = new DronePortBuilding("Drone Port", new ArrayList<>(), simulation);
    dronePortBuilding.setLocation(new Coordinate(50, 50));
    dronePort = dronePortBuilding.getDronePort();
  }

  @Test
  void test_drone_port_basic_methods() {
    assertEquals(10, dronePort.getMaxDrones());
    assertEquals(20, dronePort.getRadius());
    assertEquals(0, dronePort.getDroneCount());
    assertEquals(dronePortBuilding, dronePort.getBuilding());
  }
  
  @Test
  void test_drone_creation() {
    assertEquals(0, dronePort.getDroneCount());
    assertTrue(dronePort.createDrone());
    assertEquals(1, dronePort.getDroneCount());
  }
  
  @Test
  void test_drone_max_capacity() {
    assertEquals(0, dronePort.getDroneCount());
    assertEquals(10, dronePort.getMaxDrones());
    for (int i = 0; i < 10; i++) {
      assertTrue(dronePort.createDrone());
    }
    assertEquals(10, dronePort.getDroneCount());
    assertFalse(dronePort.createDrone());
    assertEquals(10, dronePort.getDroneCount());
  }
  
  @Test
  void test_available_drone() {
    assertNull(dronePort.getAvailableDrone());
    dronePort.createDrone();
    assertEquals(1, dronePort.getDroneCount());
    Drone drone = dronePort.getAvailableDrone();
    assertNotNull(drone);
    assertEquals(0, dronePort.getDroneCount());
    dronePort.returnDrone(drone);
    assertEquals(1, dronePort.getDroneCount());
  }
  
  @Test
  void test_return_drone_max_capacity() {
    for (int i = 0; i < 10; i++) {
      dronePort.createDrone();
    }
    assertEquals(10, dronePort.getDroneCount());
    Drone extraDrone = new Drone();
    assertFalse(dronePort.returnDrone(extraDrone));
    assertEquals(10, dronePort.getDroneCount());
  }
  
  @Test
  void test_is_within_radius() {
    Building building1 = new FactoryBuilding(new Type("Factory", new ArrayList<>()), "Factory1", new ArrayList<>(), simulation);
    building1.setLocation(new Coordinate(60, 50)); // 10 units away
    assertTrue(dronePort.isWithinRadius(building1));
    
    Building building2 = new FactoryBuilding(new Type("Factory", new ArrayList<>()), "Factory2", new ArrayList<>(), simulation);
    building2.setLocation(new Coordinate(70, 50)); // 20 units away, should be at the edge
    assertTrue(dronePort.isWithinRadius(building2));
    
    Building building3 = new FactoryBuilding(new Type("Factory", new ArrayList<>()), "Factory3", new ArrayList<>(), simulation);
    building3.setLocation(new Coordinate(71, 50)); // 21 units away, should be out of range
    assertFalse(dronePort.isWithinRadius(building3));
    
    Building building4 = new FactoryBuilding(new Type("Factory", new ArrayList<>()), "Factory4", new ArrayList<>(), simulation);
    assertFalse(dronePort.isWithinRadius(building4));
    DronePortBuilding dronePortBuilding2 = new DronePortBuilding("Drone Port 2", new ArrayList<>(), simulation);
    DronePort dronePort2 = dronePortBuilding2.getDronePort();
    assertFalse(dronePort2.isWithinRadius(building1));
  }
  
  @Test
  void test_get_and_set_drones() {
    // Create a new drone list
    ArrayList<Drone> newDrones = new ArrayList<>();
    newDrones.add(new Drone());
    newDrones.add(new Drone());
    newDrones.add(new Drone());
    
    // Test setter
    dronePort.setDrones(newDrones);
    
    // Test getter
    assertEquals(3, dronePort.getDrones().size());
    assertSame(newDrones, dronePort.getDrones());
    
    // Verify that modifications to returned list affect internal state
    dronePort.getDrones().add(new Drone());
    assertEquals(4, dronePort.getDrones().size());
  }
  
  @Test
  void test_to_json() {
    dronePort.createDrone();
    dronePort.createDrone();
    Drone drone = dronePort.getAvailableDrone();
    
    var json = dronePort.toJson();
    assertEquals(1, json.get("drones").getAsJsonArray().size());
    assertEquals(dronePortBuilding.getName(), json.get("building").getAsString());
    assertEquals(20, json.get("radius").getAsInt());
    assertEquals(10, json.get("maxDrones").getAsInt());
    
    dronePort.returnDrone(drone);
    json = dronePort.toJson();
    assertEquals(2, json.get("drones").getAsJsonArray().size());
  }

  private static class TestLogger implements Logger {
    @Override
    public void log(String message) {
    }
  }
}
