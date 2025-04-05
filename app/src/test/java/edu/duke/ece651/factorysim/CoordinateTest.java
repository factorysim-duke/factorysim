package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoordinateTest {
  Coordinate c1 = new Coordinate(0, 0);
  Coordinate c2 = new Coordinate(0, 0);
  Coordinate c3 = new Coordinate(1, 5);
  Coordinate c4 = new Coordinate(1, 5);
  Coordinate c5 = new Coordinate(5, 1);

  @Test
  public void test_get_row_and_column() {
    Coordinate testCoordinate = new Coordinate(3, 4);
    assertEquals(3, testCoordinate.getX());
    assertEquals(4, testCoordinate.getY());
  }

  @Test
  public void test_equals() {
    assertTrue(c1.equals(c1)); // should be reflexive
    assertTrue(c1.equals(c2));
    assertTrue(c3.equals(c4));
    assertFalse(c2.equals(c5));
    assertFalse(c5.equals(c4));
    assertNotEquals(c4, "(1, 5)");
    assertEquals(c2, c1);
  }

  @Test
  public void test_toString() {
    assertEquals("(0, 0)", c2.toString());
    assertEquals(c3.toString(), "(1, 5)");
  }

  @Test
  public void test_hashCode() {
    assertEquals(c1.hashCode(), c2.hashCode());
    assertEquals(c3.hashCode(), c4.hashCode());
    assertNotEquals(c1.hashCode(), c3.hashCode());
    assertNotEquals(c5.hashCode(), c4.hashCode());
  }

  @Test
  public void test_getNextCoord_invalidDirection() {
    assertThrows(IllegalArgumentException.class, () -> {
      c1.getNextCoord(4);
    });
  }
}
