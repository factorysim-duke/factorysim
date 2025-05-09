package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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

  private RecursiveLatSourcePolicy.Usage usage;
  private Item testItem;
  private List<RecursiveLatSourcePolicy.BuildingId> testPath;
  private Building building1;
  private Building building2;

  @BeforeEach
  public void setUp() {
    // set up policy
    policy = new RecursiveLatSourcePolicy();

    // set up mine buildings
    Simulation simulation = new TestUtils.MockSimulation();
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 1, 1); // lat=1
    Recipe metalRecipe = TestUtils.makeTestRecipe("metal", 2, 1); // lat=1
    Recipe waterRecipe = TestUtils.makeTestRecipe("water", 4, 1); // lat=1
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 12, 5); // lat=5 (example)

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

    Recipe handleRecipe = TestUtils.makeTestRecipe("handle", 5, 1);
    ha = new MineBuilding(handleRecipe, "Ha", simulation);
    sources.add(ha);

    Recipe shortHandleRecipe = TestUtils.makeTestRecipe("handle", 1, 1);
    MineBuilding handleFactory2 = new MineBuilding(shortHandleRecipe, "Ha2", simulation);
    sources.add(handleFactory2);

    // set up door factory
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(doorRecipe);
    Type doorFactoryType = new Type("door", recipes);
    doorFactory = new FactoryBuilding(doorFactoryType, "D", sources, simulation);

    // set up additional settings
    usage = new RecursiveLatSourcePolicy.Usage();
    testItem = new Item("testItem");
    building1 = new TestUtils.MockBuilding("B1");
    building2 = new TestUtils.MockBuilding("B2");
    testPath = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    testPath.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
  }

  @Test
  public void test_select_lowest_estimate_source() {
    Simulation simulation = new TestUtils.MockSimulation();

    Recipe longRecipe = TestUtils.makeTestRecipe("handle", 5, 1);
    MineBuilding ha = new MineBuilding(longRecipe, "Ha", simulation);
    ha.prependPendingRequest(new Request(0, new Item("handle"), longRecipe, ha, null));

    Recipe shortRecipe = TestUtils.makeTestRecipe("handle", 1, 1);
    MineBuilding ha2 = new MineBuilding(shortRecipe, "Ha2", simulation);
    ha2.prependPendingRequest(new Request(1, new Item("handle"), shortRecipe, ha2, null));
    MineBuilding ha3 = new MineBuilding(longRecipe, "Ha3", simulation);
    ha3.prependPendingRequest(new Request(2, new Item("handle"), longRecipe, ha3, null));

    List<Building> availableSources = new ArrayList<>();
    availableSources.add(ha);
    availableSources.add(ha2);
    availableSources.add(ha3);

    RecursiveLatSourcePolicy policy = new RecursiveLatSourcePolicy();
    Building selected = policy.selectSource(new Item("handle"), availableSources, (b, score) -> {
      System.out.println("Building: " + b.getName() + ", Score: " + score);
    });

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
    ironMine.prependPendingRequest(new Request(0, new Item("iron"), ironRecipe, ironMine, null));

    // bolt recipe (requires iron, lat=2)
    Recipe boltRecipe = TestUtils.makeTestRecipe("bolt", 1, 2);
    FactoryBuilding boltFactory = new FactoryBuilding(new Type("boltMaker", List.of(boltRecipe)),
        "BoltFactory", List.of(ironMine), simulation);
    Request boltRequest = new Request(1, new Item("bolt"), boltRecipe, boltFactory, null);
    boltFactory.prependPendingRequest(boltRequest);

    // door recipe (requires bolt), lat=3
    Recipe doorRecipe = TestUtils.makeTestRecipe("door", 1, 3);
    FactoryBuilding doorFactory = new FactoryBuilding(new Type("doorMaker", List.of(doorRecipe)),
        "DoorFactory", List.of(boltFactory), simulation);
    Request doorRequest = new Request(2, new Item("door"), doorRecipe, doorFactory, null);
    doorFactory.prependPendingRequest(doorRequest);

    RecursiveLatSourcePolicy policy = new RecursiveLatSourcePolicy();
    Building selected = policy.selectSource(new Item("door"), List.of(doorFactory), (b, score) -> {
    });

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
    assertTrue(id1.equals(id1)); // same object
    assertTrue(id1.equals(id2)); // same content
    assertFalse(id1.equals(null)); // null check
    assertFalse(id1.equals(new Object())); // different class
    assertFalse(id1.equals(id3)); // different uniqueId
    assertFalse(id1.equals(id4)); // different building

    // Test hashCode()
    assertEquals(id1.hashCode(), id2.hashCode()); // same content => same hash
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
    RecursiveLatSourcePolicy.RequestInUse requestInUse = new RecursiveLatSourcePolicy.RequestInUse(building1, request,
        path1);

    assertFalse(usage.isInProgressUsed(requestInUse));
    usage.addInProgressUsed(requestInUse);
    assertTrue(usage.isInProgressUsed(requestInUse));

    // Test clear reservations
    usage.clearReservations(path1);
    assertEquals(0, usage.getStorageUsed(item1, path1));
    assertFalse(usage.isInProgressUsed(requestInUse));
  }

  @Test
  public void test_add_storage_used() {
    assertEquals(0, usage.getStorageUsed(testItem, testPath));
    usage.addStorageUsed(testItem, testPath, 5);
    assertEquals(5, usage.getStorageUsed(testItem, testPath));
    usage.addStorageUsed(testItem, testPath, 3);
    assertEquals(8, usage.getStorageUsed(testItem, testPath));

    List<RecursiveLatSourcePolicy.BuildingId> otherPath = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    otherPath.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));
    assertEquals(0, usage.getStorageUsed(testItem, otherPath));
  }

  @Test
  public void test_clear_reservation() {
    List<RecursiveLatSourcePolicy.BuildingId> longPath = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    longPath.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    longPath.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));
    usage.addStorageUsed(testItem, longPath, 10);

    usage.clearReservations(testPath);
    assertEquals(0, usage.getStorageUsed(testItem, longPath));
    List<RecursiveLatSourcePolicy.BuildingId> shortPath = new ArrayList<>();
    shortPath.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    List<RecursiveLatSourcePolicy.BuildingId> longerPrefix = new ArrayList<>(shortPath);
    longerPrefix.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));
    usage.addStorageUsed(testItem, shortPath, 5);
    usage.clearReservations(longerPrefix);
    assertEquals(5, usage.getStorageUsed(testItem, shortPath));

    // don;t change the non-matching path storage
    List<RecursiveLatSourcePolicy.BuildingId> nonMatchingPath = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    nonMatchingPath.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));
    usage.addStorageUsed(testItem, nonMatchingPath, 7);
    usage.clearReservations(testPath);
    assertEquals(7, usage.getStorageUsed(testItem, nonMatchingPath));
  }

  @Test
  public void test_building_ID_equals() {
    RecursiveLatSourcePolicy.BuildingId idA = new RecursiveLatSourcePolicy.BuildingId(building1, "unique");
    RecursiveLatSourcePolicy.BuildingId idB = new RecursiveLatSourcePolicy.BuildingId(building1, "unique");
    RecursiveLatSourcePolicy.BuildingId idC = new RecursiveLatSourcePolicy.BuildingId(building1, "diff");
    RecursiveLatSourcePolicy.BuildingId idD = new RecursiveLatSourcePolicy.BuildingId(building2, "unique");

    assertTrue(idA.equals(idA));
    assertFalse(idA.equals(null));
    assertFalse(idA.equals(3));
    assertTrue(idA.equals(idB));
    assertEquals(idA.hashCode(), idB.hashCode());
    assertFalse(idA.equals(idC));
    assertFalse(idA.equals(idD));
  }

  @Test
  public void test_request_in_use_equals() {
    Recipe testRecipe = TestUtils.makeTestRecipe("test", 5, 1);
    Request request1 = new Request(1, new Item("test"), testRecipe, building1, null);
    Request request2 = new Request(2, new Item("test"), testRecipe, building1, null);
    List<RecursiveLatSourcePolicy.BuildingId> pathA = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    pathA.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    List<RecursiveLatSourcePolicy.BuildingId> pathB = new ArrayList<RecursiveLatSourcePolicy.BuildingId>();
    pathB.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    pathB.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));

    RecursiveLatSourcePolicy.RequestInUse riu1 = new RecursiveLatSourcePolicy.RequestInUse(building1, request1, pathA);
    RecursiveLatSourcePolicy.RequestInUse riu2 = new RecursiveLatSourcePolicy.RequestInUse(building1, request1, pathA);
    RecursiveLatSourcePolicy.RequestInUse riu3 = new RecursiveLatSourcePolicy.RequestInUse(building1, request2, pathA);
    RecursiveLatSourcePolicy.RequestInUse riu4 = new RecursiveLatSourcePolicy.RequestInUse(building1, request1, pathB);

    assertTrue(riu1.equals(riu1));
    assertFalse(riu1.equals(null));
    assertFalse(riu1.equals(2));
    assertTrue(riu1.equals(riu2));
    assertEquals(riu1.hashCode(), riu2.hashCode());
    assertFalse(riu1.equals(riu3));
    assertFalse(riu1.equals(riu4));
  }

  @Test
  public void test_estimate_with_equal_current_request() {
    Recipe recipe = TestUtils.makeTestRecipe("test", 10, 0);
    Request request = new Request(100, new Item("test"), recipe, building1, null);
    building1.setCurrentRequest(request);
    int estimate = policy.estimate(request, building1, usage, testPath);
    assertEquals(10, estimate);
  }

  @Test
  public void test_estimate_with_efficient_storage() {
    Item dog = new Item("dog");
    LinkedHashMap<Item, Integer> ingredients = new LinkedHashMap<Item, Integer>();
    ingredients.put(dog, 5);
    Recipe recipe = new Recipe(new Item("dog"), ingredients, 7);
    Request request = new Request(999, new Item("dog"), recipe, building1, null);
    building1.addToStorage(dog, 5);
    building1.setCurrentRequest(null);
    int estimate = policy.estimate(request, building1, usage, testPath);
    assertEquals(7, estimate);
  }

  @Test
  public void test_estimate_with_missing_ingredients() {
    Item cat = new Item("cat");
    LinkedHashMap<Item, Integer> ingredients = new LinkedHashMap<Item, Integer>();
    ingredients.put(cat, 2);
    Recipe recipe = new Recipe(new Item("dog"), ingredients, 4);
    Request request = new Request(99999, new Item("dog"), recipe, building1, null);
    building1.setCurrentRequest(null);
    building1.updateSources(new ArrayList<Building>());
    int estimate = policy.estimate(request, building1, usage, testPath);
    assertEquals(4, estimate);
  }

  // test source building of cat
  private static class TestSourceBuilding extends TestUtils.MockBuilding {
    private Item ingredient;
    private int latency;

    public TestSourceBuilding(String name, Item ingredient, int latency) {
      super(name);
      this.ingredient = ingredient;
      this.latency = latency;
    }

    @Override
    public Recipe getRecipeForItem(Item item) {
      return TestUtils.makeTestRecipe("cat", latency, 0);
    }
  }

  @Test
  public void test_estimate_with_missing_ingredients_with_source() {
    Item cat = new Item("cat");
    LinkedHashMap<Item, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put(cat, 3);
    Recipe recipe = new Recipe(new Item("dog"), ingredients, 5);
    Request request = new Request(10, new Item("dog"), recipe, building1, null);
    building1.addToStorage(cat, 1);
    building1.setCurrentRequest(null);
    TestSourceBuilding source1 = new TestSourceBuilding("Source1", cat, 2);
    TestSourceBuilding source2 = new TestSourceBuilding("Source2", cat, 2);
    TestSourceBuilding source3 = new TestSourceBuilding("Source3", cat, 2);
    List<Building> newSources = new ArrayList<>();
    newSources.add(source1);
    newSources.add(source2);
    newSources.add(source3);
    building1.updateSources(newSources);

    int estimate = policy.estimate(request, building1, usage, testPath);
    assertTrue(estimate > 5);
  }

  @Test
  public void test_prefix_longer_than_path() {
    List<RecursiveLatSourcePolicy.BuildingId> shortPath = new ArrayList<>();
    shortPath.add(new RecursiveLatSourcePolicy.BuildingId(building1, "id1"));
    List<RecursiveLatSourcePolicy.BuildingId> longerPrefix = new ArrayList<>(shortPath);
    longerPrefix.add(new RecursiveLatSourcePolicy.BuildingId(building2, "id2"));
    usage.addStorageUsed(testItem, shortPath, 5);
    usage.clearReservations(longerPrefix);
    assertEquals(5, usage.getStorageUsed(testItem, shortPath));
  }

  @Test
  public void test_estimate_storage_with_stock() {
    Item testItem = new Item("screw");
    Recipe screwRecipe = TestUtils.makeTestRecipe("screw", 3, 0);

    TestUtils.MockSimulation mockSim = new TestUtils.MockSimulation() {
      @Override
      public Recipe getRecipeForItem(Item item) {
        return screwRecipe;
      }
    };

    StorageBuilding storageBuilding = new StorageBuilding(
        "ScrewStorage",
        new ArrayList<>(),
        mockSim,
        testItem,
        10,
        1.0);
    storageBuilding.addToStorage(testItem, 5);
    Request request = new Request(1, testItem, screwRecipe, storageBuilding, null);
    List<RecursiveLatSourcePolicy.BuildingId> storagePath = new ArrayList<>();
    storagePath.add(new RecursiveLatSourcePolicy.BuildingId(storageBuilding, "storage-id"));
    RecursiveLatSourcePolicy.Usage storageUsage = new RecursiveLatSourcePolicy.Usage();
    int estimate = policy.estimate(request, storageBuilding, storageUsage, storagePath);
    assertEquals(0, estimate);
    assertEquals(1, storageUsage.getStorageUsed(testItem, storagePath));
  }

  @Test
  public void test_estimate_storage_multiple_requests() {
    Item testItem = new Item("bolt");
    TestUtils.MockSimulation mockSim = new TestUtils.MockSimulation() {
      @Override
      public Recipe getRecipeForItem(Item item) {
        return TestUtils.makeTestRecipe("bolt", 3, 0);
      }
    };

    StorageBuilding storageBuilding = new StorageBuilding(
        "BoltStorage",
        new ArrayList<>(),
        mockSim,
        testItem,
        10,
        1.0);
    storageBuilding.addToStorage(testItem, 2);
    Recipe recipe = TestUtils.makeTestRecipe("bolt", 3, 0);
    Request request = new Request(1, testItem, recipe, storageBuilding, null);
    List<RecursiveLatSourcePolicy.BuildingId> storagePath = new ArrayList<>();
    storagePath.add(new RecursiveLatSourcePolicy.BuildingId(storageBuilding, "storage-id"));
    RecursiveLatSourcePolicy.Usage storageUsage = new RecursiveLatSourcePolicy.Usage();
    int estimate1 = policy.estimate(request, storageBuilding, storageUsage, storagePath);
    assertEquals(0, estimate1);
    assertEquals(1, storageUsage.getStorageUsed(testItem, storagePath));
    int estimate2 = policy.estimate(request, storageBuilding, storageUsage, storagePath);
    assertEquals(0, estimate2);
    assertEquals(2, storageUsage.getStorageUsed(testItem, storagePath));
    int estimate3 = policy.estimate(request, storageBuilding, storageUsage, storagePath);
    assertTrue(estimate3 > 0); // Not immediately available
  }

  @Test
  public void test_estimate_storage_different_item() {
    Item storageItem = new Item("nail");
    StorageBuilding storageBuilding = new StorageBuilding(
        "NailStorage",
        new ArrayList<>(),
        new TestUtils.MockSimulation(),
        storageItem,
        10,
        1.0);
    storageBuilding.addToStorage(storageItem, 5);
    Item requestItem = new Item("screw");
    Recipe recipe = TestUtils.makeTestRecipe("screw", 3, 0);
    Request request = new Request(1, requestItem, recipe, storageBuilding, null);
    List<RecursiveLatSourcePolicy.BuildingId> storagePath = new ArrayList<>();
    storagePath.add(new RecursiveLatSourcePolicy.BuildingId(storageBuilding, "storage-id"));
    RecursiveLatSourcePolicy.Usage storageUsage = new RecursiveLatSourcePolicy.Usage();
    int estimate = policy.estimate(request, storageBuilding, storageUsage, storagePath);
    assertTrue(estimate > 0);
    assertEquals(0, storageUsage.getStorageUsed(storageItem, storagePath));
  }
}
