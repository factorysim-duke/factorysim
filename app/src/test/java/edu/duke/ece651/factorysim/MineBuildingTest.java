package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class MineBuildingTest {
  @Test
  public void test_constructor_and_getters() {
    // {("a", 1), ("b", 2), ("c", 3), ("d", 4)}
    HashMap<Item, Integer> ingredients = TestUtils.makeTestIngredientMap(4);
    Item out = new Item("out");
    Recipe recipe = new Recipe(out, ingredients, 6);
    MineBuilding mine = new MineBuilding(recipe, "myMine");
    assertSame(out, mine.getResource());
    assertEquals(6, mine.getMiningLatency());
    assertEquals("myMine", mine.getName());
  }

}
