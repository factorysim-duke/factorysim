package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import org.junit.jupiter.api.Test;

public class WorldBuilderAdditionalTest {

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

}
