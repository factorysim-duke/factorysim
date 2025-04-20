package edu.duke.ece651.factorysim;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a generic event handler class.
 *
 * @param <T> is the type being sent with the event when an event happens.
 */
public class EventHandler<T> {
    private final List<Consumer<T>> listeners = new ArrayList<>();

    /**
     * Adds a new listener to the event handler.
     *
     * @param listener is the new event listener.
     */
    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from the event handler.
     *
     * @param listener is the event listener to remove.
     */
    public void unsubscribe(Consumer<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Invokes the event.
     *
     * @param data is the data instance to be sent with the event.
     */
    public void invoke(T data) {
        for (Consumer<T> listener : listeners) {
            listener.accept(data);
        }
    }

    /**
     * Unsubscribes all event listeners from this event handler.
     */
    public void clear() {
        listeners.clear();
    }
}
