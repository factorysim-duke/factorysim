package edu.duke.ece651.factorysim;

public class Delivery {
    final Building source;
    final Building destination;
    final Item item;
    final int quantity;
    int deliveryTime;

    public Delivery(Building source, Building destination, Item item, int quantity, int deliveryTime) {
        this.source = source;
        this.destination = destination;
        this.item = item;
        this.quantity = quantity;
        this.deliveryTime = deliveryTime;
    }

    public void step() {
        if (deliveryTime > 0) {
            deliveryTime--;
        }
    }

    public boolean isArrive() {
        return deliveryTime == 0;
    }

    public void finishDelivery() {
        destination.addToStorage(item, quantity);
        source.getSimulation().onIngredientDelivered(item, destination, source);
    }

}
