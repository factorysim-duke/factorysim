package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
  public void test_processing_waste() {
    wasteDisposal.addToStorage(sawdust, 51);
    wasteDisposal.step();
    assertEquals(51, wasteDisposal.getStorageNumberOf(sawdust));
    wasteDisposal.step();
    assertEquals(1, wasteDisposal.getStorageNumberOf(sawdust));
    wasteDisposal.step();
    assertEquals(1, wasteDisposal.getStorageNumberOf(sawdust));
    wasteDisposal.step();
    assertEquals(-1, wasteDisposal.getStorageNumberOf(sawdust));
  }

  @Test
  public void test_partial_processing() {
    wasteDisposal.addToStorage(sawdust, 75);
    wasteDisposal.step();
    wasteDisposal.step();
    assertEquals(25, wasteDisposal.getStorageNumberOf(sawdust));
    wasteDisposal.step();
    wasteDisposal.step();
    assertEquals(-1, wasteDisposal.getStorageNumberOf(sawdust));
    wasteDisposal.step();
    wasteDisposal.step();
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
}
