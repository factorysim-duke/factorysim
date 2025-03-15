package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

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
  public void test_WorldBuilder_failure_missingRecipe() {
    ConfigData configDataMissingRecipe = TestUtils.loadConfigData("src/test/resources/inputs/MissingReceipe.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingRecipe, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_missingIngredient() {
    ConfigData configDataMissingIngredient = TestUtils.loadConfigData("src/test/resources/inputs/TypeRecipeMissingIngredient.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingIngredient, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_mineRecipeHasIngredients() {
    ConfigData configDataMineRecipeHasIngredients = TestUtils.loadConfigData("src/test/resources/inputs/MineReceipeHasIngredients.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMineRecipeHasIngredients, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_buildingNotMineOrFactory() {
    ConfigData configDataBuildingNotMineOrFactory = TestUtils.loadConfigData("src/test/resources/inputs/BuildingNotMineOrFactory.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataBuildingNotMineOrFactory, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_buildingTypeNotDefined() {
    ConfigData configDataBuildingTypeNotDefined = TestUtils.loadConfigData("src/test/resources/inputs/BuildingTypeNotDefined.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataBuildingTypeNotDefined, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_sourceBuildingNotDefined() {
    ConfigData configDataSourceBuildingNotDefined = TestUtils.loadConfigData("src/test/resources/inputs/SourceBuildingNotDefined.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataSourceBuildingNotDefined, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_mineBuildingHasSource() {
    ConfigData configDataMineBuildingHasSource = TestUtils.loadConfigData("src/test/resources/inputs/MineBuildingHasSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMineBuildingHasSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_factoryHasNoSource() {
    ConfigData configDataFactoryHasNoSource = TestUtils.loadConfigData("src/test/resources/inputs/FactoryHasNoSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataFactoryHasNoSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_factoryMissingOneSource() {
    ConfigData configDataFactoryMissingOneSource = TestUtils.loadConfigData("src/test/resources/inputs/FactoryMissingOneSource.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataFactoryMissingOneSource, new TestUtils.MockSimulation());
    });
  }
}