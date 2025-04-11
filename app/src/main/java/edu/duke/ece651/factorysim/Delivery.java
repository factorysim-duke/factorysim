package edu.duke.ece651.factorysim;

import com.google.gson.JsonObject;
/**
 * Represents a delivery of a specific item from a source building to a destination building.
 * Each delivery contains an item, its quantity, and a remaining delivery time.
 */
public class Delivery {
    final Building source;
    final Building destination;
    final Item item;
    final int quantity;
    int deliveryTime;
    /**
     * Constructs a Delivery object with specified source, destination, item, quantity, and delivery time.
     *
     * @param source the source building from which the item originates
     * @param destination the destination building to which the item is delivered
     * @param item the item being delivered
     * @param quantity the number of items being delivered
     * @param deliveryTime the number of cycles required for the delivery to complete
     */
    public Delivery(Building source, Building destination, Item item, int quantity, int deliveryTime) {
        this.source = source;
        this.destination = destination;
        this.item = item;
        this.quantity = quantity;
        this.deliveryTime = deliveryTime;
    }

    /**
     * Decreases the delivery time by one cycle, simulating one timestep in the simulation.
     */
    public void step() {
        if (deliveryTime > 0) {
            deliveryTime--;
        }
    }

    /**
     * Checks whether the delivery has arrived at its destination.
     *
     * @return true if deliveryTime is 0, indicating arrival; false otherwise
     */
    public boolean isArrive() {
        return deliveryTime == 0;
    }

    /**
     * Completes the delivery by adding the item to the destination's storage and notifying the simulation.
     * Should only be called when the delivery has arrived.
     */
    public void finishDelivery() {
        destination.addToStorage(item, quantity);
        source.getSimulation().onIngredientDelivered(item, destination, source);
    }

    /**
     * Converts the delivery details to a JSON object for serialization or display.
     *
     * @return a JsonObject representing the delivery with source, destination, item name, quantity, and delivery time
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("source", source.getName());
        json.addProperty("destination", destination.getName());
        json.addProperty("item", item.getName());
        json.addProperty("quantity", quantity);
        json.addProperty("deliveryTime", deliveryTime);
        return json;
    }
}
