package edu.duke.ece651.factorysim;

import java.util.HashMap;

public class TestUtils {
  /**
   * Conviniently generates a hashmap for ingredients.
   * Used in recipe constructor and thus in type, building, ...
   * The form will be like {("a", 1), ("b", 2), ("c", 3), ...}
   * 
   * @param num is the number of key-value pairs you want in the map. Please make
   *            it between 1-25.
   * @return the generated ingredients map used for testing.
   */
  public static HashMap<Item, Integer> makeTestIngredientMap(int num) {
    char start = 'a';
    HashMap<Item, Integer> ans = new HashMap<>();
    for (int i = 0; i < num; i++) {
      char cur = (char) (start + i);
      String name = String.valueOf(cur);
      Item item = new Item(name);
      ans.put(item, i + 1);
    }
    return ans;
  }
}
