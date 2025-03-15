package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class WorldTest {
  @Test
  public void test_getBuildingFromName() {
    World world = WorldBuilder.buildWorld(TestUtils.loadConfigData("src/test/resources/inputs/doors1.json"), new TestUtils.MockSimulation());
    assertEquals(world.getBuildingFromName("D").getName(), "D");
  }

  @Test
  public void test_getBuildingFromName_null() {
    World world = WorldBuilder.buildWorld(TestUtils.loadConfigData("src/test/resources/inputs/doors1.json"), new TestUtils.MockSimulation());
    assertNull(world.getBuildingFromName("Z"));
  }
}
