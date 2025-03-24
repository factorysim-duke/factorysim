package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecursiveLatSourcePolicyTest {
  private RecursiveLatSourcePolicy policy;
  private List<Building> sources;
  private MineBuilding woodMine;
  private MineBuilding metalMine1;
  private MineBuilding metalMine2;
  private MineBuilding waterMine1;
  private MineBuilding waterMine2;
  private MineBuilding ha;
  private FactoryBuilding doorFactory;

  @BeforeEach
  public void setUp() {
    // set up policy
    policy = new RecursiveLatSourcePolicy();

    // set up mine buildings
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 1);
    Recipe metalRecipe = TestUtils.makeTestRecipe("metal", 2, 1);
    Recipe waterRecipe = TestUtils.makeTestRecipe("water", 4, 1);
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 5);
    woodMine = new MineBuilding(woodRecipe, "W", simulation);
    metalMine1 = new MineBuilding(metalRecipe, "M1", simulation);
    metalMine2 = new MineBuilding(metalRecipe, "M2", simulation);
    waterMine1 = new MineBuilding(waterRecipe, "W1", simulation);
    waterMine2 = new MineBuilding(waterRecipe, "W2", simulation);
    
    // set up sources
    sources = new ArrayList<>();
    sources.add(woodMine);
    sources.add(metalMine1);
    sources.add(metalMine2);
    sources.add(waterMine1);
    sources.add(waterMine2);
    
    Recipe handleRecipe = TestUtils.makeTestRecipe("handle", 5, 1);
    ha = new MineBuilding(handleRecipe, "Ha", simulation);
    sources.add(ha);
    
    // 添加第二个 handle 工厂，延迟更短
    Recipe shortHandleRecipe = TestUtils.makeTestRecipe("handle", 1, 1);
    MineBuilding handleFactory2 = new MineBuilding(shortHandleRecipe, "Ha2", simulation);
    sources.add(handleFactory2);

    // set up factory
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(doorRecipe);
    Type doorFactoryType = new Type("door", recipes);
    doorFactory = new FactoryBuilding(doorFactoryType, "D", sources, simulation);
  }

  @Test
  public void test_select_lowest_estimate_source() {
    Simulation simulation = new TestUtils.MockSimulation();
  
    Recipe longRecipe = TestUtils.makeTestRecipe("handle", 5, 1);
    MineBuilding ha = new MineBuilding(longRecipe, "Ha", simulation);
    ha.addPendingRequest(new Request(0, new Item("handle"), longRecipe, ha, null));
  
    Recipe shortRecipe = TestUtils.makeTestRecipe("handle", 1, 1);
    MineBuilding ha2 = new MineBuilding(shortRecipe, "Ha2", simulation);
    ha2.addPendingRequest(new Request(1, new Item("handle"), shortRecipe, ha2, null));
  
    List<Building> availableSources = new ArrayList<>();
    availableSources.add(ha);
    availableSources.add(ha2);
  
    RecursiveLatSourcePolicy policy = new RecursiveLatSourcePolicy();
    Building selected = policy.selectSource(new Item("handle"), availableSources);
  
    assertEquals("Ha2", selected.getName());
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
  public void test_get_name() {
    assertEquals("recursivelat", policy.getName());
  }

}
