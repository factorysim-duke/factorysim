package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WorldTest {
  @Test
  public void test_get_methods() {
    World world = WorldBuilder.buildWorld(TestUtils.loadConfigData("src/test/resources/inputs/doors1.json"), new TestUtils.MockSimulation());
    assertEquals(world.getBuildingFromName("D").getName(), "D");
    assertNotNull(world.getTypeFromName("hinge"));
    assertNull(world.getTypeFromName("h"));
    assertNotNull(world.getRecipeFromName("door"));
    assertNull(world.getRecipeFromName("doo"));
  }

  @Test
  public void test_getBuildingFromName_null() {
    World world = WorldBuilder.buildWorld(TestUtils.loadConfigData("src/test/resources/inputs/doors1.json"), new TestUtils.MockSimulation());
    assertNull(world.getBuildingFromName("Z"));
  }

  @Test
  public void test_isOccupied() {
    World world = new World();
    world.locationMap = new HashMap<>();
    world.locationMap.put(null, new Coordinate(3, 7)); // Simulate being occupied
    assertTrue(world.isOccupied(new Coordinate(3, 7)));
    assertFalse(world.isOccupied(new Coordinate(1, 2)));
  }

  @Test
  public void test_tryAddBuilding() {
    World world = new World();
    world.setBuildings(new ArrayList<>());
    world.locationMap = new HashMap<>();
    world.locationMap.put(null, new Coordinate(2, 5)); // Simulate being occupied

    Building deez = new TestUtils.MockBuilding("Deez");
    deez.setLocation(new Coordinate(2, 5));
    assertFalse(world.tryAddBuilding(deez));

    deez.setLocation(new Coordinate(1, 2));
    assertTrue(world.tryAddBuilding(deez));

    world.tileMap = new TileMap(10, 10);
    deez.setLocation(new Coordinate(3, 3));
    assertTrue(world.tryAddBuilding(deez));
  }

  @Test
  public void test_resolveBuildingNameConflict() {
    World world = WorldBuilder.buildWorld(TestUtils.loadConfigData("src/test/resources/inputs/doors1.json"), new TestUtils.MockSimulation());
    assertEquals("deez", world.resolveBuildingNameConflict("deez"));
    assertEquals("D_1", world.resolveBuildingNameConflict("D"));
  }
}
