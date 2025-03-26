package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SjfRequestPolicyTest {
  private SjfRequestPolicy policy;
  private Building producer;
  private List<Request> requests;

  @BeforeEach
  public void setUp() {
    policy = new SjfRequestPolicy();
    producer = new TestUtils.MockBuilding("TestBuilding");
    requests = new ArrayList<>();
  }

  @Test
  public void testSelectRequestEmptyList() {
    Request result = policy.selectRequest(producer, requests);
    assertNull(result, "Should return null for empty request list");
  }

  @Test
  public void testSelectRequestSingleItem() {
    Item output = new Item("TestItem");
    HashMap<Item, Integer> ingredients = new HashMap<>();
    Recipe recipe = new Recipe(output, ingredients, 5);
    Request request = new Request(1, output, recipe, producer, null);
    requests.add(request);
    Request result = policy.selectRequest(producer, requests);
    assertEquals(request, result);
  }

  @Test
  public void testSelectRequestMultipleItems() {
    Item item1 = new Item("Item1");
    Item item2 = new Item("Item2");
    Item item3 = new Item("Item3");

    HashMap<Item, Integer> ingredients = new HashMap<>();

    Recipe recipe1 = new Recipe(item1, ingredients, 10);
    Recipe recipe2 = new Recipe(item2, ingredients, 5); // This is the shortest job we should return
    Recipe recipe3 = new Recipe(item3, ingredients, 15);

    Request request1 = new Request(1, item1, recipe1, producer, null);
    Request request2 = new Request(2, item2, recipe2, producer, null);
    Request request3 = new Request(3, item3, recipe3, producer, null);

    requests.add(request1);
    requests.add(request2);
    requests.add(request3);

    Request result = policy.selectRequest(producer, requests);

    assertEquals(request2, result);
    assertEquals(3, requests.size());
    assertTrue(requests.contains(request1));
    assertTrue(requests.contains(request3));
  }

  @Test
  public void testSelectRequestEqualLatencies() {
    Item item1 = new Item("Item1");
    Item item2 = new Item("Item2");

    HashMap<Item, Integer> ingredients = new HashMap<>();

    // Create two recipes with the same latency, so we should break the tie by returning the first one since it's the first one in the list
    Recipe recipe1 = new Recipe(item1, ingredients, 10);
    Recipe recipe2 = new Recipe(item2, ingredients, 10); 

    Request request1 = new Request(1, item1, recipe1, producer, null);
    Request request2 = new Request(2, item2, recipe2, producer, null);

    requests.add(request1);
    requests.add(request2);

    Request result = policy.selectRequest(producer, requests);

    assertEquals(request1, result);
    assertEquals(2, requests.size());
    assertTrue(requests.contains(request2));
  }

  @Test
  public void test_getName() {
    assertEquals("sjf", policy.getName());
  }
}
