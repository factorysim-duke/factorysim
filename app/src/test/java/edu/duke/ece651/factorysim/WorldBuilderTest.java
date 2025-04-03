package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class WorldBuilderTest {
  @Test
  public void test_WorldBuilder_success() {
    ConfigData configDataDoors1 = TestUtils.loadConfigData("src/test/resources/inputs/doors1.json");
    World world = WorldBuilder.buildWorld(configDataDoors1, new TestUtils.MockSimulation());
    assertEquals(world.getBuildings().size(), 5);
    assertEquals(world.getTypes().size(), 3);
    assertEquals(world.getRecipes().size(), 5);
  }

  @Test
  public void test_WorldBuilder_with_storage() {
    ConfigData configData = TestUtils.loadConfigData("src/test/resources/inputs/storageBuilder.json");
    assertNotNull(configData, "ConfigData should not be null");

    Simulation simulation = new Simulation("src/test/resources/inputs/storageBuilder.json");

    World world = WorldBuilder.buildWorld(configData, simulation);
    Building hingeFactory = world.getBuildingFromName("Hi");
    assertNotNull(hingeFactory, "Hi should exist");
    assertTrue(hingeFactory instanceof FactoryBuilding);

    assertEquals(2, hingeFactory.getStorage().getOrDefault(new Item("metal"), 0));
    assertEquals(3, hingeFactory.getStorage().getOrDefault(new Item("hinge"), 0));

    simulation.step(1);
  }

  @Test
  public void test_WorldBuilder_failure_missingRecipe() {
    ConfigData configDataMissingRecipe = TestUtils.loadConfigData("src/test/resources/inputs/MissingReceipe.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingRecipe, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_missingIngredient() {
    ConfigData configDataMissingIngredient = TestUtils
        .loadConfigData("src/test/resources/inputs/TypeRecipeMissingIngredient.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingIngredient, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_mineRecipeHasIngredients() {
    ConfigData configDataMineRecipeHasIngredients = TestUtils
        .loadConfigData("src/test/resources/inputs/MineReceipeHasIngredients.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMineRecipeHasIngredients, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_buildingNotMineOrFactory() {
    ConfigData configDataBuildingNotMineOrFactory = TestUtils
        .loadConfigData("src/test/resources/inputs/BuildingNotMineOrFactory.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataBuildingNotMineOrFactory, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_buildingTypeNotDefined() {
    ConfigData configDataBuildingTypeNotDefined = TestUtils
        .loadConfigData("src/test/resources/inputs/BuildingTypeNotDefined.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataBuildingTypeNotDefined, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_sourceBuildingNotDefined() {
    ConfigData configDataSourceBuildingNotDefined = TestUtils
        .loadConfigData("src/test/resources/inputs/SourceBuildingNotDefined.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataSourceBuildingNotDefined, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_mineBuildingHasSource() {
    ConfigData configDataMineBuildingHasSource = TestUtils
        .loadConfigData("src/test/resources/inputs/MineBuildingHasSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMineBuildingHasSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_factoryHasNoSource() {
    ConfigData configDataFactoryHasNoSource = TestUtils
        .loadConfigData("src/test/resources/inputs/FactoryHasNoSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataFactoryHasNoSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_factoryMissingOneSource() {
    ConfigData configDataFactoryMissingOneSource = TestUtils
        .loadConfigData("src/test/resources/inputs/FactoryMissingOneSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataFactoryMissingOneSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_complete_auto_location_assignment() {
    // no building was provided a location
    ConfigData configData = TestUtils.loadConfigData("src/test/resources/inputs/NoLocationAssigned.json");
    Simulation simulation = new TestUtils.MockSimulation();
    World world = WorldBuilder.buildWorld(configData, simulation);

    Building D = world.getBuildingFromName("D");
    Building Ha = world.getBuildingFromName("Ha");
    Building Hi = world.getBuildingFromName("Hi");
    Building W = world.getBuildingFromName("W");
    Building M = world.getBuildingFromName("M");
    assertNotNull(D);
    assertNotNull(Ha);
    assertNotNull(Hi);
    assertNotNull(W);
    assertNotNull(M);

    // first building should be assigned (0,0)
    assertEquals(new Coordinate(0, 0), D.getLocation());

    // verify the remaining building placements
    for (Building b : new Building[] { Ha, Hi, W, M }) {
      Coordinate location = b.getLocation();
      assertNotNull(location, b.getName());
      int diffX = Math.abs(location.getX());
      int diffY = Math.abs(location.getY());
      assertTrue(diffX >= 5, b.getName());
      assertTrue(diffY >= 5, b.getName());
    }
  }

  @Test
  public void test_partial_auto_location_assignment() {
    // some buildings are provided initial locations
    ConfigData configData = TestUtils.loadConfigData("src/test/resources/inputs/SomeLocationAssigned.json");
    Simulation sim = new TestUtils.MockSimulation();
    World world = WorldBuilder.buildWorld(configData, sim);

    Building D = world.getBuildingFromName("D");
    Building Ha = world.getBuildingFromName("Ha");
    Building Hi = world.getBuildingFromName("Hi");
    Building W = world.getBuildingFromName("W");
    Building M = world.getBuildingFromName("M");
    assertEquals(new Coordinate(20, 20), D.getLocation());
    assertEquals(new Coordinate(25, 25), Hi.getLocation());
    assertEquals(new Coordinate(10, 10), W.getLocation());
    assertEquals(new Coordinate(15, 10), M.getLocation());

    // Ha should get a new location
    Coordinate newLocation = Ha.getLocation();
    assertNotNull(newLocation);

    // Ha should be 5 units away from all other buildings
    for (Building b : new Building[] { D, Hi, W, M }) {
      int diffX = Math.abs(newLocation.getX() - b.getLocation().getX());
      int diffY = Math.abs(newLocation.getY() - b.getLocation().getY());
      assertTrue(diffX >= 5);
      assertTrue(diffY >= 5);
    }

    // Ha should be within 10 units of at least one building
    boolean withinX = false;
    boolean withinY = false;
    for (Building b : new Building[] { D, Hi, W, M }) {
      int diffX = Math.abs(newLocation.getX() - b.getLocation().getX());
      int diffY = Math.abs(newLocation.getY() - b.getLocation().getY());
      if (diffX <= 10) {
        withinX = true;
      }
      if (diffY <= 10) {
        withinY = true;
      }
    }
    assertTrue(withinX);
    assertTrue(withinY);

    // extra testing for distance functions
    Set<Coordinate> usedCoordinates = new HashSet<>();
    usedCoordinates.add(D.getLocation()); // (20, 20)
    assertFalse(WorldBuilder.isNotTooCloseToOthers(new Coordinate(16, 24), usedCoordinates));
    assertFalse(WorldBuilder.isNotTooFarFromOthers(new Coordinate(9, 20), usedCoordinates));
  }

  @Test
  public void test_initial_duplicate_locations() {
    // two buildings are given initial duplicated locations
    ConfigData configData = TestUtils.loadConfigData("src/test/resources/inputs/DuplicateLocation.json");
    Simulation sim = new TestUtils.MockSimulation();
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configData, sim);
    });
  }
}
