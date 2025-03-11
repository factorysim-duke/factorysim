package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class WorldBuilderTest {
  ConfigData configDataDoors1 = TestUtils.loadConfigData("src/test/resources/inputs/doors1.json");
  ConfigData configDataDoors2 = TestUtils.loadConfigData("src/test/resources/inputs/doors2.json");

  @Test
  public void test_WorldBuilder_success() {
    World world = WorldBuilder.buildWorld(configDataDoors1);
    assertEquals(world.getBuildings().size(), 5);
    assertEquals(world.getTypes().size(), 3);
    assertEquals(world.getRecipes().size(), 5);
  }
}
