package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

}
