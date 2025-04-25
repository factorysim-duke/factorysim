package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class WorldBuilderTest {

  /* --------- Basic/Empty World Tests --------- */

  @Test
  public void test_buildEmptyWorld_default() {
    World w = WorldBuilder.buildEmptyWorld();
    assertNotNull(w, "World should not be null");
    assertEquals(0, w.getTypes().size(), "Types list should be empty");
    assertEquals(0, w.getRecipes().size(), "Recipes list should be empty");
    assertEquals(0, w.getBuildings().size(), "Buildings list should be empty");
  }

  @Test
  public void test_buildEmptyWorld_customSize() {
    World w = WorldBuilder.buildEmptyWorld(123, 45);
    assertNotNull(w, "Custom size empty world should not be null");
    assertTrue(w.getTypes().isEmpty());
    assertTrue(w.getRecipes().isEmpty());
    assertTrue(w.getBuildings().isEmpty());
  }

  /* --------- Location Placement Tests --------- */

  @Test
  public void test_isNotTooCloseToOthers_true() {
    Set<Coordinate> used = new HashSet<>();
    used.add(new Coordinate(0, 0));
    assertTrue(WorldBuilder.isNotTooCloseToOthers(new Coordinate(5, 5), used));
  }

  @Test
  public void test_isNotTooCloseToOthers_false() {
    Set<Coordinate> used = new HashSet<>();
    used.add(new Coordinate(0, 0));
    assertFalse(WorldBuilder.isNotTooCloseToOthers(new Coordinate(4, 10), used));
    assertFalse(WorldBuilder.isNotTooCloseToOthers(new Coordinate(10, 4), used));
  }

  @Test
  public void test_isNotTooFarFromOthers_true() {
    Set<Coordinate> used = new HashSet<>();
    used.add(new Coordinate(0, 0));
    assertTrue(WorldBuilder.isNotTooFarFromOthers(new Coordinate(9, 9), used));
  }

  @Test
  public void test_isNotTooFarFromOthers_false() {
    Set<Coordinate> used = new HashSet<>();
    used.add(new Coordinate(0, 0));
    assertFalse(WorldBuilder.isNotTooFarFromOthers(new Coordinate(11, 0), used));
    assertFalse(WorldBuilder.isNotTooFarFromOthers(new Coordinate(0, 11), used));
  }

  @Test
  public void test_findValidLocation_viaReflection() throws Exception {
    Method m = WorldBuilder.class.getDeclaredMethod("findValidLocation", Set.class);
    m.setAccessible(true);

    Coordinate loc1 = (Coordinate) m.invoke(null, Collections.emptySet());
    assertNull(loc1, "When there are no used coordinates, findValidLocation should return null");

    Set<Coordinate> used = new HashSet<>();
    used.add(new Coordinate(0, 0));
    Coordinate loc2 = (Coordinate) m.invoke(null, used);
    assertNotNull(loc2, "When there are used coordinates, findValidLocation should return a valid location");
    assertTrue(WorldBuilder.isNotTooCloseToOthers(loc2, used));
    assertTrue(WorldBuilder.isNotTooFarFromOthers(loc2, used));
  }

  /* --------- World Building Tests --------- */

  @Test
  public void test_buildWorld_emptyConfig_customBoard() {
    ConfigData cfg = new ConfigData();
    cfg.buildings = Collections.emptyList();
    cfg.types     = Collections.emptyList();
    cfg.recipes   = Collections.emptyList();
    cfg.connections     = Collections.emptyList();
    cfg.wasteDisposals  = Collections.emptyList();

    Simulation sim = new TestUtils.MockSimulation();
    World w = WorldBuilder.buildWorld(cfg, sim, 77, 88);
    assertNotNull(w);
    assertEquals(0, w.getBuildings().size());
    assertEquals(0, w.getTypes().size());
    assertEquals(0, w.getRecipes().size());
  }

  @Test
  public void test_buildWorld_emptyConfig_defaultBoard() {
    ConfigData cfg = new ConfigData();
    cfg.buildings = Collections.emptyList();
    cfg.types     = Collections.emptyList();
    cfg.recipes   = Collections.emptyList();
    cfg.connections     = Collections.emptyList();
    cfg.wasteDisposals  = Collections.emptyList();

    Simulation sim = new TestUtils.MockSimulation();
    World w = WorldBuilder.buildWorld(cfg, sim);
    assertNotNull(w);
    assertTrue(w.getBuildings().isEmpty());
    assertTrue(w.getTypes().isEmpty());
    assertTrue(w.getRecipes().isEmpty());
  }

  @Test
  public void test_buildWorld_invalidWasteConfigs() {
    ConfigData cfg = new ConfigData();
    cfg.buildings = Collections.emptyList();
    cfg.types     = Collections.emptyList();
    cfg.recipes   = Collections.emptyList();
    cfg.connections    = Collections.emptyList();

    WasteDisposalDTO dto = new WasteDisposalDTO();
    dto.name = "WD1";
    dto.x = 1; dto.y = 1;
    dto.wasteTypes = new LinkedHashMap<>();
    WasteDisposalDTO.WasteConfig badCap = new WasteDisposalDTO.WasteConfig();
    badCap.capacity = 0; badCap.disposalRate = 5; badCap.timeSteps = 2;
    dto.wasteTypes.put("itemA", badCap);

    cfg.wasteDisposals = List.of(dto);

    Simulation sim = new TestUtils.MockSimulation();
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(cfg, sim);
    }, "Should throw an exception due to invalid capacity");
    
    dto.wasteTypes.clear();
    WasteDisposalDTO.WasteConfig badRate = new WasteDisposalDTO.WasteConfig();
    badRate.capacity = 10; badRate.disposalRate = 0; badRate.timeSteps = 2;
    dto.wasteTypes.put("itemB", badRate);
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(cfg, sim);
    }, "Should throw an exception due to invalid disposal rate");
    
    dto.wasteTypes.clear();
    WasteDisposalDTO.WasteConfig badTime = new WasteDisposalDTO.WasteConfig();
    badTime.capacity = 10; badTime.disposalRate = 5; badTime.timeSteps = 0;
    dto.wasteTypes.put("itemC", badTime);
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(cfg, sim);
    }, "Should throw an exception due to invalid time steps");
  }

  /* --------- Integration Tests from WorldBuilderTest --------- */
  
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
    assertDoesNotThrow(() -> {
      WorldBuilder.buildWorld(configDataFactoryHasNoSource, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_failure_factoryMissingOneSource() {
    ConfigData configDataFactoryMissingOneSource = TestUtils
        .loadConfigData("src/test/resources/inputs/FactoryMissingOneSource.json");
    assertDoesNotThrow(() -> {
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
    assertEquals(new Coordinate(5, 5), D.getLocation());
    assertEquals(new Coordinate(19, 19), Hi.getLocation());
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
    usedCoordinates.add(D.getLocation()); // (5, 5)
    assertFalse(WorldBuilder.isNotTooCloseToOthers(new Coordinate(1, 1), usedCoordinates));
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

  @Test
  public void test_WorldBuilder_storage_missing_capacity_or_priority() {
    ConfigData configDataMissingCapacity = TestUtils
        .loadConfigData("src/test/resources/inputs/StorageMissingCapacity.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingCapacity, new TestUtils.MockSimulation());
    });
    ConfigData configDataMissingPriority = TestUtils
        .loadConfigData("src/test/resources/inputs/StorageMissingPriority.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingPriority, new TestUtils.MockSimulation());
    });
    ConfigData configDataMissingBoth = TestUtils.loadConfigData("src/test/resources/inputs/StorageMissingFields.json");
    assertThrows(IllegalArgumentException.class, () -> {
      WorldBuilder.buildWorld(configDataMissingBoth, new TestUtils.MockSimulation());
    });
  }

  @Test
  public void test_WorldBuilder_storage_success() {
    ConfigData configData = TestUtils.loadConfigData("src/main/resources/doors_with_storage.json");
    assertNotNull(configData);
    World world = WorldBuilder.buildWorld(configData, new TestUtils.MockSimulation());
    Building storageBuilding = world.getBuildingFromName("WS");
    assertNotNull(storageBuilding);
    assertTrue(storageBuilding instanceof StorageBuilding);
    StorageBuilding woodStorage = (StorageBuilding) storageBuilding;
    assertEquals("wood", woodStorage.getStorageItem().getName());
  }

  @Test
  public void test_WorldBuilder_waste_disposal() {
    ConfigData configData = TestUtils.loadConfigData("src/main/resources/electronics_with_waste.json");
    assertNotNull(configData, "ConfigData should not be null");
    Simulation simulation = new Simulation("src/main/resources/electronics_with_waste.json");
    World world = simulation.getWorld();
    Building woodWasteDisposal = world.getBuildingFromName("wood_waste_disposal");
    Building electronicsWasteDisposal = world.getBuildingFromName("electronics_waste_disposal");
    assertNotNull(woodWasteDisposal);
    assertNotNull(electronicsWasteDisposal);
    assertTrue(woodWasteDisposal instanceof WasteDisposalBuilding);
    assertTrue(electronicsWasteDisposal instanceof WasteDisposalBuilding);
    
    WasteDisposalBuilding woodDisposal = (WasteDisposalBuilding) woodWasteDisposal;
    WasteDisposalBuilding electronicsDisposal = (WasteDisposalBuilding) electronicsWasteDisposal;
    List<Item> woodWasteTypes = woodDisposal.getWasteTypes();
    List<Item> electronicsWasteTypes = electronicsDisposal.getWasteTypes();
    assertEquals(1, woodWasteTypes.size());
    assertEquals(2, electronicsWasteTypes.size());
    
    Item sawdust = new Item("sawdust");
    Item electronicWaste = new Item("electronic_waste");
    Item plasticScraps = new Item("plastic_scraps");
    assertTrue(woodDisposal.canProduce(sawdust));
    assertEquals(400, woodDisposal.getMaxCapacityFor(sawdust));
    assertEquals(50, woodDisposal.getDisposalRateFor(sawdust));
    assertEquals(2, woodDisposal.getDisposalTimeStepsFor(sawdust));
    assertTrue(electronicsDisposal.canProduce(electronicWaste));
    assertTrue(electronicsDisposal.canProduce(plasticScraps));
    assertEquals(300, electronicsDisposal.getMaxCapacityFor(electronicWaste));
    assertEquals(200, electronicsDisposal.getMaxCapacityFor(plasticScraps));
    assertEquals(30, electronicsDisposal.getDisposalRateFor(electronicWaste));
    assertEquals(25, electronicsDisposal.getDisposalRateFor(plasticScraps));
    assertEquals(3, electronicsDisposal.getDisposalTimeStepsFor(electronicWaste));
    assertEquals(1, electronicsDisposal.getDisposalTimeStepsFor(plasticScraps));
    assertEquals(new Coordinate(50, 40), woodDisposal.getLocation());
    assertEquals(new Coordinate(90, 40), electronicsDisposal.getLocation());
  }

  @Test
  public void test_WorldBuilder_drone_port() {
    ConfigData configData = TestUtils.loadConfigData("src/test/resources/inputs/dronePort.json");
    assertNotNull(configData, "ConfigData should not be null");
    Simulation simulation = new TestUtils.MockSimulation();
    World world = WorldBuilder.buildWorld(configData, simulation);
    
    Building dronePort = world.getBuildingFromName("DroneStation");
    assertNotNull(dronePort, "DroneStation should exist");
    assertTrue(dronePort instanceof DronePortBuilding);
    
    DronePortBuilding portBuilding = (DronePortBuilding) dronePort;
    DronePort port = portBuilding.getDronePort();
    assertNotNull(port, "DronePort should exist");
    
    List<Drone> drones = port.getDrones();
    assertNotNull(drones, "Drones list should not be null");
    assertEquals(2, drones.size(), "Should have 2 drones");
    
    // First drone should be in use, second should not be
    assertTrue(drones.get(0).isInUse(), "First drone should be in use");
    assertFalse(drones.get(1).isInUse(), "Second drone should not be in use");
  }

  /* --------- Branch Coverage Tests --------- */

  @DisplayName("buildWorld • invalid config ⇒ IllegalArgumentException")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
    "MissingReceipe.json",
    "TypeRecipeMissingIngredient.json",
    "MineReceipeHasIngredients.json",
    "BuildingNotMineOrFactory.json",
    "BuildingTypeNotDefined.json",
    "SourceBuildingNotDefined.json",
    "MineBuildingHasSource.json",
    "StorageMissingCapacity.json",
    "StorageMissingPriority.json",
    "StorageMissingFields.json"
  })
  void buildWorld_illegalConfigs(String fname) {
    ConfigData bad = TestUtils.loadConfigData(path(fname));
    assertThrows(IllegalArgumentException.class,
        () -> WorldBuilder.buildWorld(bad, new TestUtils.MockSimulation()));
  }

  @ParameterizedTest(name = "waste-disposal with bad {0} ⇒ IllegalArgumentException")
  @CsvSource({
    "capacity,waste_disposal_bad_capacity.json", 
    "rate,waste_disposal_bad_rate.json", 
    "timestep,waste_disposal_bad_timestep.json"
  })
  void wasteDisposal_invalidFields(String field, String filename) {
    ConfigData bad = TestUtils.loadConfigData(path(filename));
    assertThrows(IllegalArgumentException.class,
        () -> WorldBuilder.buildWorld(bad, new TestUtils.MockSimulation()));
  }

  /* --------- Connection Tests --------- */

  /** 
   * A simple ConnectionDTO constructor that uses reflection to set private fields
   */
  private ConnectionDTO mkDto(String src, String dst) {
    ConnectionDTO dto = new ConnectionDTO();
    try {
      Field f1 = ConnectionDTO.class.getDeclaredField("source");
      f1.setAccessible(true);
      f1.set(dto, src);
      Field f2 = ConnectionDTO.class.getDeclaredField("destination");
      f2.setAccessible(true);
      f2.set(dto, dst);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return dto;
  }

  /**
   * A StubSimulation that captures logs and connectBuildings calls
   */
  static class StubSimulation extends TestUtils.MockSimulation {
    World world; 
    int verbosity;
    List<String> logs = new ArrayList<>();
    boolean connected = false;
    String srcConnected, dstConnected;

    StubSimulation(World world, int verbosity) {
      super();
      this.world = world;
      this.verbosity = verbosity;
    }

    @Override public World getWorld() { return world; }
    @Override public int getVerbosity() { return verbosity; }
    @Override public Logger getLogger() {
      return msg -> logs.add(msg);
    }
    @Override public boolean connectBuildings(String src, String dst) {
      connected = true;
      srcConnected = src;
      dstConnected = dst;
      return true;
    }
  }

  /**
   * Calls the private buildConnections method using reflection
   */
  private void callBuildConnections(List<ConnectionDTO> dtos, StubSimulation sim) throws Exception {
    Method m = WorldBuilder.class
      .getDeclaredMethod("buildConnections", List.class, Simulation.class);
    m.setAccessible(true);
    m.invoke(null, dtos, sim);
  }

  @Test
  public void worldNotReady_logsWarning() throws Exception {
    StubSimulation sim = new StubSimulation(null, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Warning: World not set"));
    assertFalse(sim.connected);
  }

  @Test
  public void missingSource_logsAndSkip() throws Exception {
    // world exists but buildings list is empty
    World w = WorldBuilder.buildEmptyWorld();
    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("X","Y")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Source building 'X' does not exist"));
    assertFalse(sim.connected);
  }

  @Test
  public void missingDestination_logsAndSkip() throws Exception {
    // world has only "A" building
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));

    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","Z")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Destination building 'Z' does not exist"));
    assertFalse(sim.connected);
  }

  @Test
  public void connectThrows_logsError() throws Exception {
    // world has A and B buildings
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));
    w.getBuildings().add(new TestUtils.MockBuilding("B"));

    StubSimulation sim = new StubSimulation(w, 1) {
      @Override
      public boolean connectBuildings(String src, String dst) {
        throw new IllegalArgumentException("boom");
      }
    };
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertEquals(1, sim.logs.size());
    assertTrue(sim.logs.get(0).contains("Failed to create connection from A to B: boom"));
    assertFalse(sim.connected);
  }

  @Test
  public void successfulConnect_noLogs() throws Exception {
    // world has A and B buildings
    World w = WorldBuilder.buildEmptyWorld();
    w.getBuildings().add(new TestUtils.MockBuilding("A"));
    w.getBuildings().add(new TestUtils.MockBuilding("B"));

    StubSimulation sim = new StubSimulation(w, 1);
    callBuildConnections(Collections.singletonList(mkDto("A","B")), sim);

    assertTrue(sim.connected);
    assertEquals("A", sim.srcConnected);
    assertEquals("B", sim.dstConnected);
    assertTrue(sim.logs.isEmpty());
  }

  @Test
  public void test_buildConnections_fullMatrix() throws Exception {
    // miniature world with two buildings
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    World tiny = WorldBuilder.buildEmptyWorld();
    Building A = new StorageBuilding("A", List.of(), sim, new Item("x"), 1, 1);
    Building B = new StorageBuilding("B", List.of(), sim, new Item("y"), 1, 1);
    tiny.setBuildings(List.of(A, B));
    sim.setWorld(tiny);            // make world visible to builder

    // build a connection list that hits every branch:
    //  1) missing source   (C→A)
    //  2) missing dest     (A→C)
    //  3) connect ok       (A→B)
    //  4) connect throws   (B→A) – we throw manually from MockSimulation
    List<ConnectionDTO> dto = Arrays.asList(
        new ConnectionDTO("C", "A"),
        new ConnectionDTO("A", "C"),
        new ConnectionDTO("A", "B"),
        new ConnectionDTO("B", "A"));

    // tell mock to throw when we try B→A
    sim.throwOn("B", "A");

    // call the *private* method via reflection
    Method m = WorldBuilder.class.getDeclaredMethod(
        "buildConnections", List.class, Simulation.class);
    m.setAccessible(true);
    m.invoke(null, dto, sim);

    // only the valid one (A→B) should have been registered
    assertEquals(List.of("A->B"), sim.connections());
  }
  
  @Test
  public void test_buildConnections_nullWorld() throws Exception {
    TestUtils.MockSimulation sim = new TestUtils.MockSimulation();
    // Don't set world on simulation to test null world branch
    
    List<ConnectionDTO> dto = Collections.singletonList(new ConnectionDTO("A", "B"));
    
    Method m = WorldBuilder.class.getDeclaredMethod(
        "buildConnections", List.class, Simulation.class);
    m.setAccessible(true);
    m.invoke(null, dto, sim);
    
    // No connections should be made
    assertTrue(sim.connections().isEmpty());
  }

  /* --------- Helper methods --------- */

  private static String path(String fname) {
    return "src/test/resources/inputs/" + fname;
  }
}
