package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleLatSourcePolicyTest {
  private SimpleLatSourcePolicy policy;
  private List<Building> sources;
  private MineBuilding woodMine;
  private MineBuilding metalMine1;
  private MineBuilding metalMine2;
  private MineBuilding waterMine1;
  private MineBuilding waterMine2;
  private MineBuilding waterMine3;
  private FactoryBuilding doorFactory;

  @BeforeEach
  public void setUp() {
    // set up policy
    policy = new SimpleLatSourcePolicy();

    // set up mine buildings
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 1);
    Recipe metalRecipe = TestUtils.makeTestRecipe("metal", 2, 1);
    Recipe waterRecipe = TestUtils.makeTestRecipe("water", 2, 1);
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 5);
    woodMine = new MineBuilding(woodRecipe, "W", simulation);
    metalMine1 = new MineBuilding(metalRecipe, "M1", simulation);
    metalMine2 = new MineBuilding(metalRecipe, "M2", simulation);
    waterMine1 = new MineBuilding(waterRecipe, "W1", simulation);
    waterMine2 = new MineBuilding(waterRecipe, "W2", simulation);
    waterMine3 = new MineBuilding(waterRecipe, "W3", simulation);

    // set up sources
    sources = new ArrayList<>();
    sources.add(woodMine);
    sources.add(metalMine1);
    sources.add(metalMine2);
    sources.add(waterMine1);
    sources.add(waterMine2);
    sources.add(waterMine3);

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

  @Test
  public void test_equal_latency() {
    Item metal = new Item("metal");
    metalMine1.addRequest(new Request(0, metal, metalMine1.getMiningRecipe(), metalMine1, doorFactory));
    metalMine2.addRequest(new Request(1, metal, metalMine2.getMiningRecipe(), metalMine2, doorFactory));
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(metal);
    Building result = policy.selectSource(metal, availableSources);
    assertSame(metalMine1, result);
  }

  @Test
  public void test_min_latency() {
    Item water = new Item("water");
    // add 2 requests to water mine 1
    waterMine1.addRequest(new Request(2, water, waterMine1.getMiningRecipe(), waterMine1, doorFactory));
    waterMine1.addRequest(new Request(3, water, waterMine1.getMiningRecipe(), metalMine1, doorFactory));
    // add 1 request to water mine 2
    waterMine2.addRequest(new Request(5, water, waterMine2.getMiningRecipe(), waterMine2, doorFactory));
    // add 1 requests to water mine 3
    waterMine3.addRequest(new Request(6, water, waterMine3.getMiningRecipe(), waterMine3, doorFactory));
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(water);
    Building result = policy.selectSource(water, availableSources);
    assertSame(waterMine2, result);

    // process one request in water mine 3
    waterMine3.processRequestEasyVersion();
    Building result1 = policy.selectSource(water, availableSources);
    assertSame(waterMine3, result1);
  }
}
