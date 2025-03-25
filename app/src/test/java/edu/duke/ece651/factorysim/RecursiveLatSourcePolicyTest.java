package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 1);   // lat=1
    Recipe metalRecipe = TestUtils.makeTestRecipe("metal", 2, 1); // lat=1
    Recipe waterRecipe = TestUtils.makeTestRecipe("water", 4, 1); // lat=1
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 5);  // lat=5 (example)
    
    woodMine = new MineBuilding(woodRecipe, "W", simulation);
    metalMine1 = new MineBuilding(metalRecipe, "M1", simulation);
    metalMine2 = new MineBuilding(metalRecipe, "M2", simulation);
    waterMine1 = new MineBuilding(waterRecipe, "W1", simulation);
    waterMine2 = new MineBuilding(waterRecipe, "W2", simulation);

    sources = new ArrayList<>();
    sources.add(woodMine);
    sources.add(metalMine1);
    sources.add(metalMine2);
    sources.add(waterMine1);
    sources.add(waterMine2);

    // Add a handle factory: recipe "handle"
    Recipe handleRecipe = TestUtils.makeTestRecipe("handle", 5, 1);
    ha = new MineBuilding(handleRecipe, "Ha", simulation);
    sources.add(ha);

    // Another "handle" factory, different latency
    Recipe shortHandleRecipe = TestUtils.makeTestRecipe("handle", 1, 1);
    MineBuilding handleFactory2 = new MineBuilding(shortHandleRecipe, "Ha2", simulation);
    sources.add(handleFactory2);

    // set up door factory
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
    Building selected = policy.selectSource(new Item("handle"), availableSources, (b, score) -> {
      // score reporting for coverage
      // System.out.println("Building: " + b.getName() + ", Score: " + score);
    });

    // ha2 has a smaller total estimate (1 vs 5), so should be chosen
    assertEquals("Ha2", selected.getName());
  }

  @Test
  public void test_select_from_empty_sources() {
    Item invalid_item = new Item("invalid");
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(invalid_item);
    Building result = policy.selectSource(invalid_item, availableSources);
    assertNull(result); // no source can produce "invalid"
  }

  @Test
  public void test_select_single_possibility() {
    Item wood = new Item("wood");
    List<Building> availableSources = doorFactory.getAvailableSourcesForItem(wood);
    Building result = policy.selectSource(wood, availableSources);
    // The only wood producer from the setUp is woodMine ("W")
    assertSame(woodMine, result);
  }

  @Test
  public void test_get_name() {
    assertEquals("recursivelat", policy.getName());
  }

  @Test
  public void test_estimate_full_coverage() {
    Simulation simulation = new TestUtils.MockSimulation();

    // iron recipe
    Recipe ironRecipe = TestUtils.makeTestRecipe("iron", 1, 0);
    MineBuilding ironMine = new MineBuilding(ironRecipe, "IronMine", simulation);
    ironMine.addPendingRequest(new Request(0, new Item("iron"), ironRecipe, ironMine, null));

    // bolt recipe (requires iron, lat=2)
    Recipe boltRecipe = TestUtils.makeTestRecipe("bolt", 1, 2);
    FactoryBuilding boltFactory = new FactoryBuilding(new Type("boltMaker", List.of(boltRecipe)), 
                                                      "BoltFactory", List.of(ironMine), simulation);
    Request boltRequest = new Request(1, new Item("bolt"), boltRecipe, boltFactory, null);
    boltFactory.addPendingRequest(boltRequest);

    // door recipe (requires bolt?), lat=3
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 1, 3);
    FactoryBuilding doorFactory = new FactoryBuilding(new Type("doorMaker", List.of(doorRecipe)), 
                                                      "DoorFactory", List.of(boltFactory), simulation);
    Request doorRequest = new Request(2, new Item("door"), doorRecipe, doorFactory, null);
    doorFactory.addPendingRequest(doorRequest);

    RecursiveLatSourcePolicy policy = new RecursiveLatSourcePolicy();
    Building selected = policy.selectSource(new Item("door"), List.of(doorFactory), (b, score) -> {});

    // Only one building => doorFactory, so we expect "DoorFactory"
    assertEquals("DoorFactory", selected.getName());
  }

  @Test
  public void test_building_id_equals_and_hashcode() {
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe recipe = TestUtils.makeTestRecipe("test", 1, 1);
    Building building1 = new MineBuilding(recipe, "B1", simulation);
    Building building2 = new MineBuilding(recipe, "B2", simulation);
    
    RecursiveLatSourcePolicy.BuildingId id1 = new RecursiveLatSourcePolicy.BuildingId(building1, "unique1");
    RecursiveLatSourcePolicy.BuildingId id2 = new RecursiveLatSourcePolicy.BuildingId(building1, "unique1");
    RecursiveLatSourcePolicy.BuildingId id3 = new RecursiveLatSourcePolicy.BuildingId(building1, "unique2");
    RecursiveLatSourcePolicy.BuildingId id4 = new RecursiveLatSourcePolicy.BuildingId(building2, "unique1");
    
    // Test equals()
    assertTrue(id1.equals(id1));  // same object
    assertTrue(id1.equals(id2));  // same content
    assertFalse(id1.equals(null));  // null check
    assertFalse(id1.equals(new Object()));  // different class
    assertFalse(id1.equals(id3));  // different uniqueId
    assertFalse(id1.equals(id4));  // different building
    
    // Test hashCode()
    assertEquals(id1.hashCode(), id2.hashCode());  // same content => same hash
    assertNotEquals(id1.hashCode(), id3.hashCode()); 
  }

  @Test
  public void test_usage() {
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe recipe = TestUtils.makeTestRecipe("test", 1, 1);
    Building building1 = new MineBuilding(recipe, "B1", simulation);
    Building building2 = new MineBuilding(recipe, "B2", simulation);
    
    RecursiveLatSourcePolicy.Usage usage = new RecursiveLatSourcePolicy.Usage();
    
    // Test storage usage
    Item item1 = new Item("item1");
    List<RecursiveLatSourcePolicy.BuildingId> path1 = Arrays.asList(
        new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    
    usage.addStorageUsed(item1, path1, 5);
    assertEquals(5, usage.getStorageUsed(item1, path1));
    assertEquals(0, usage.getStorageUsed(item1, Arrays.asList(
        new RecursiveLatSourcePolicy.BuildingId(building2, "id1"))));
    
    // Test request in progress
    Request request = new Request(1, item1, recipe, building1, null);
    RecursiveLatSourcePolicy.RequestInUse requestInUse = 
        new RecursiveLatSourcePolicy.RequestInUse(building1, request, path1);
    
    assertFalse(usage.isInProgressUsed(requestInUse));
    usage.addInProgressUsed(requestInUse);
    assertTrue(usage.isInProgressUsed(requestInUse));
    
    // Test clear reservations
    usage.clearReservations(path1);
    assertEquals(0, usage.getStorageUsed(item1, path1));
    assertFalse(usage.isInProgressUsed(requestInUse));
  }
}