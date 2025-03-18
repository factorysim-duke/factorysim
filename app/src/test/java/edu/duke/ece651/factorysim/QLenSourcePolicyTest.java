package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QLenSourcePolicyTest {
  private QLenSourcePolicy policy;
  private List<Building> sources;
  MineBuilding woodMine;
  MineBuilding metalMine1;
  MineBuilding metalMine2;
  MineBuilding metalMine3;
  private FactoryBuilding doorFactory;

  @BeforeEach
  public void setUp() {
    // set up policy
    policy = new QLenSourcePolicy();

    // set up mine buildings
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 1);
    Recipe metalRecipe = TestUtils.makeTestRecipe("metal", 1, 1);
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 5);
    woodMine = new MineBuilding(woodRecipe, "W", simulation);
    metalMine1 = new MineBuilding(metalRecipe, "M1", simulation);
    metalMine2 = new MineBuilding(metalRecipe, "M2", simulation);
    metalMine3 = new MineBuilding(metalRecipe, "M3", simulation);

    // set up sources
    sources = new ArrayList<>();
    sources.add(metalMine1);
    sources.add(metalMine2);
    sources.add(metalMine3);
    sources.add(woodMine);

    // set up factory
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(doorRecipe);
    Type doorFactoryType = new Type("door", recipes);
    doorFactory = new FactoryBuilding(doorFactoryType, "D", sources, simulation);
  }

  @Test
  public void test_select_from_empty_sources() {
    Item invalid_item = new Item("invalid");
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(invalid_item);
    Building result = policy.selectSource(invalid_item, availableSources);
    assertNull(result);
  }

  @Test
  public void test_select_single_possibility() {
    Item wood = new Item("wood");
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(wood);
    Building result = policy.selectSource(wood, availableSources);
    assertSame(woodMine, result);
  }
}
