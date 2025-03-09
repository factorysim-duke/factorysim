package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ItemTest {
  @Test
  public void test_get_name() {
    Item wood = new Item("wood");
    assertEquals("wood", wood.getName());
    Item water = new Item("wATeR");
    assertEquals("wATeR", water.getName());
    Item gba = new Item("game machine");
    assertEquals("game machine", gba.getName());
  }

  @Test
  public void test_invalid_name() {
    assertThrows(IllegalArgumentException.class, () -> new Item("woo'd"));
    assertThrows(IllegalArgumentException.class, () -> new Item("wood'"));
    assertThrows(IllegalArgumentException.class, () -> new Item("'A"));
    assertThrows(IllegalArgumentException.class, () -> new Item("'"));
  }
}
