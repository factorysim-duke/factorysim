package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class DronePortBuildingTest {
  private World world;
  private Simulation simulation;
  private DronePortBuilding dronePortBuilding;

  @BeforeEach
  void setUp() {
    world = new World();
    simulation = new Simulation(world, 0, new TestLogger());
    dronePortBuilding = new DronePortBuilding("Drone Port", new ArrayList<>(), simulation);
    dronePortBuilding.setLocation(new Coordinate(50, 50));
  }

  @Test
  void test_drone_port_building_methods() {
    Item item = new Item("TestItem");
    assertFalse(dronePortBuilding.canProduce(item));
    assertTrue(dronePortBuilding.canBeRemovedImmediately());
    
    HashMap<Item, Integer> ingredients = new HashMap<>();
    Recipe recipe = new Recipe(item, ingredients, 1);
    Request request = new Request(1, item, recipe, dronePortBuilding, dronePortBuilding);
    dronePortBuilding.prependPendingRequest(request);
    assertFalse(dronePortBuilding.canBeRemovedImmediately());
    
    assertEquals(0, dronePortBuilding.getDroneCount());
    assertTrue(dronePortBuilding.createDrone());
    assertEquals(1, dronePortBuilding.getDroneCount());
    assertEquals(10, dronePortBuilding.getMaxDrones());
    
    var json = dronePortBuilding.toJson();
    assertEquals("DronePort", json.get("type").getAsString());
    assertEquals("Drone Port", json.get("name").getAsString());
    assertEquals(1, json.get("droneCount").getAsInt());
    assertEquals(10, json.get("maxDrones").getAsInt());
    assertEquals(20, json.get("radius").getAsInt());
    assertEquals(50, json.get("x").getAsInt());
    assertEquals(50, json.get("y").getAsInt());
    
    assertTrue(json.has("sources"));
    assertEquals(0, json.getAsJsonArray("sources").size());
    assertTrue(json.has("drones"));
    assertEquals(1, json.getAsJsonArray("drones").size());
  }
  
  private static class TestLogger implements Logger {
    @Override
    public void log(String message) {
    }
  }
}
