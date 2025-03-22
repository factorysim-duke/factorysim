package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TupleTest {
  @Test
  public void test_constructor_getters() {
    Tuple<String, Integer> t1 = new Tuple<>("one", 1);
    assertEquals("one", t1.first());
    assertEquals(1, t1.second());

    Tuple<Boolean, Double> t2 = new Tuple<>(true, 3.14);
    assertTrue(t2.first());
    assertEquals(3.14, t2.second());

    Tuple<Object, Object> t3 = new Tuple<>(null, null);
    assertNull(t3.first());
    assertNull(t3.second());

    Tuple<String, Tuple<Integer, Integer>> t4 = new Tuple<>("(1, 2)", new Tuple<>(1, 2));
    assertEquals("(1, 2)", t4.first());
    assertEquals(new Tuple<>(1, 2), t4.second());
  }

  @Test
  public void test_equals() {
    Tuple<String, Integer> t1 = new Tuple<>("key", 42);
    Tuple<String, Integer> t2 = new Tuple<>("key", 42);
    Tuple<String, Integer> t3 = new Tuple<>("key", 43);
    Tuple<String, Integer> t4 = new Tuple<>("notKey", 42);
    Tuple<String, String> t5 = new Tuple<>("key", "42");

    assertEquals(t1, t1);
    assertNotEquals(t1, null);
    assertNotEquals(t1, "string");
    assertEquals(t1, t2);
    assertNotEquals(t1, t3);
    assertNotEquals(t1, t4);
    assertNotEquals(t1, t5);
  }

  @Test
  public void test_hashCode() {
    Tuple<String, Integer> t1 = new Tuple<>("x", 99);
    Tuple<String, Integer> t2 = new Tuple<>("x", 99);
    Tuple<String, Integer> t3 = new Tuple<>("x", 100);

    assertEquals(t1.hashCode(), t2.hashCode());
    assertNotEquals(t1.hashCode(), t3.hashCode());
  }

  @Test
  public void test_toString() {
    Tuple<String, Integer> t1 = new Tuple<>("abc", 123);
    assertEquals("(abc, 123)", t1.toString());

    Tuple<Object, Object> t2 = new Tuple<>(null, null);
    assertEquals("(null, null)", t2.toString());

    Tuple<Boolean, String> t3 = new Tuple<>(false, "xyz");
    assertEquals("(false, xyz)", t3.toString());

    Tuple<String, Tuple<Integer, Integer>> t4 = new Tuple<>("(7, 8)", new Tuple<>(7, 8));
    assertEquals("((7, 8), (7, 8))", t4.toString());
  }
}
