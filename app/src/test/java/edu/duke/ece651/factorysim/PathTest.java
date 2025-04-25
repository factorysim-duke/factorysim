package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PathTest {
  @Test
  public void test_toString() {
    Path path = new Path();
    Coordinate start = new Coordinate(0, 0);
    Coordinate middle = new Coordinate(1, 0);
    Coordinate end = new Coordinate(2, 0);
    
    // Add coordinates to path (start → middle → end)
    path.emplaceBack(start, false, 1); // start, not new, direction right
    path.emplaceBack(middle, true, 1); // middle, new tile, direction right
    path.emplaceBack(end, false, 1); // end, not new, direction right
    
    String expected = "Path from (0, 0) to (2, 0), total steps=3, newTiles=1, cost=4";
    assertEquals(expected, path.toString());
  }
  
  @Test
  public void test_pathMethods() {
    Path path = new Path();
    
    // Create a path with 4 steps, 2 new tiles
    path.emplaceBack(new Coordinate(0, 0), false, 1);
    path.emplaceBack(new Coordinate(1, 0), true, 1);
    path.emplaceBack(new Coordinate(2, 0), true, 1);
    path.emplaceBack(new Coordinate(3, 0), false, 1);
    
    // Test getters
    assertEquals(4, path.getTotalLength());
    assertEquals(2, path.getNewTileCount());
    assertEquals(6, path.getCost()); // 4 (length) + 2 (new tiles)
    assertEquals(2, path.getDeliveryTime()); // total length - 2
    
    // Test isMatch
    assertTrue(path.isMatch(new Coordinate(0, 0), new Coordinate(3, 0)));
    assertFalse(path.isMatch(new Coordinate(0, 1), new Coordinate(3, 0)));
    assertFalse(path.isMatch(new Coordinate(0, 0), new Coordinate(3, 1)));
  }
}
