package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReadyRequestPolicyTest {
  @Test
  public void test_popRequest() {
    RequestPolicy requestPolicy = new ReadyRequestPolicy();

    Recipe steelRecipe = TestUtils.makeTestRecipe("steel", 3, 2);
    MineBuilding steelMine = new MineBuilding(steelRecipe, "steel mine", requestPolicy);

    // Doable request
    Item steel = new Item("steel");
    List<Request> requests = new ArrayList<>(List.of(new Request(1, steel, steelRecipe, steelMine, null)));
    requestPolicy.popRequest(steelMine, requests);
    assertTrue(requests.isEmpty()); // Because `popRequest` popped the only doable request

    // Undoable request
    Item chair = new Item("chair");
    HashMap<Item, Integer> ingredients = new HashMap<>();
    ingredients.put(new Item("wood"), 3);
    Recipe chairRecipe = new Recipe(chair, ingredients, 1);
    requests = new ArrayList<>(List.of(new Request(1, chair, chairRecipe, steelMine, null)));
    requestPolicy.popRequest(steelMine, requests);
    assertFalse(requests.isEmpty()); // Because `steelMine` cannot produce `chair` so no request was popped
  }
}
