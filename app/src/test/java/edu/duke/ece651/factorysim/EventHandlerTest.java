package edu.duke.ece651.factorysim;

import java.util.*;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventHandlerTest {
    @Test
    void test_subscribe() {
        EventHandler<Integer> handler = new EventHandler<>();
        List<Integer> received = new ArrayList<>();

        Consumer<Integer> listener = received::add;
        handler.subscribe(listener);
        handler.invoke(10);

        assertEquals(1, received.size());
        assertEquals(10, received.getFirst());
    }

    @Test
    void test_unsubscribe() {
        EventHandler<Integer> handler = new EventHandler<>();
        List<Integer> received = new ArrayList<>();

        Consumer<Integer> listener = received::add;
        handler.subscribe(listener);
        handler.unsubscribe(listener);
        handler.invoke(20);

        assertTrue(received.isEmpty());
    }

    @Test
    void test_invoke() {
        EventHandler<Integer> handler = new EventHandler<>();
        List<Integer> received = new ArrayList<>();

        Consumer<Integer> listener1 = received::add;
        Consumer<Integer> listener2 = (i) -> received.add(i * 2);

        handler.subscribe(listener1);
        handler.subscribe(listener2);
        handler.invoke(5);

        assertEquals(2, received.size());
        assertTrue(received.contains(5));
        assertTrue(received.contains(10));
    }

    @Test
    void test_clear() {
        EventHandler<Integer> handler = new EventHandler<>();
        List<Integer> received = new ArrayList<>();

        Consumer<Integer> listener = received::add;
        handler.subscribe(listener);
        handler.clear();
        handler.invoke(30);

        assertTrue(received.isEmpty());
    }
}
