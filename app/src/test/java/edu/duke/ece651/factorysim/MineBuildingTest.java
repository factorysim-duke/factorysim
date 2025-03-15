package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MineBuildingTest {
  @Test
  public void test_constructor_and_getters() {
    Item out = new Item("out");
    Recipe recipe = new Recipe(out, new HashMap<>(), 6);
    MineBuilding mine = new MineBuilding(recipe, "myMine", new TestUtils.MockSimulation());
    assertSame(out, mine.getResource());
    assertEquals(6, mine.getMiningLatency());
    assertEquals("myMine", mine.getName());
    assertSame(recipe, mine.getMiningRecipe());
  }

  @Test
  public void test_processRequest() {
    Item wood = new Item("wood");
    Recipe woodRecipe = TestUtils.makeTestRecipe("wood", 3, 2);
    MineBuilding mine = new MineBuilding(woodRecipe, "woodMine", new TestUtils.MockSimulation());

    FactoryBuilding factory = new FactoryBuilding(new Type("Chair Factory",
            new ArrayList<>()), "Chair Factory", List.of(mine), new TestUtils.MockSimulation());

    // Mine shouldn't be processing any request now
    assertFalse(mine.isProcessing());

    // Add request
    Request request = new Request(1, wood, woodRecipe, mine, factory);
    mine.addRequest(request);

    // Mine shouldn't be finished since there's a pending request
    assertFalse(mine.isFinished());

    // Request is not complete initially because recipe latency is 3
    assertFalse(request.isCompleted());

    // Process once
    mine.processRequest(); // Request remaining steps = 2
    assertFalse(request.isCompleted()); // Request should be incomplete
    assertTrue(mine.isProcessing()); // Mine should be processing the request
    assertFalse(mine.isFinished()); // Mine should not be finished since it's processing the request

    // Process once again
    mine.processRequest(); // Request remaining steps = 1
    assertFalse(request.isCompleted()); // Request should be incomplete
    assertTrue(mine.isProcessing()); // Mine should still be processing the request
    assertFalse(mine.isFinished()); // Mine should not be finished since it's processing the request

    // Process once again again
    mine.processRequest(); // Request remaining steps = 0
    assertTrue(request.isCompleted()); // Request should be completed now
    assertFalse(mine.isProcessing()); // Mine shouldn't be processing now

    // Mine should be finished now since there's no current and pending requests
    assertTrue(mine.isFinished());

    // This `processRequest` should not change anything since there's no pending requests
    mine.processRequest();
    assertTrue(request.isCompleted());
    assertFalse(mine.isProcessing());
  }
}
