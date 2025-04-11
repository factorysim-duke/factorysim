package edu.duke.ece651.factorysim;

import java.util.ArrayList;
import java.util.List;

public class DeliverySchedule {
    List<Delivery> deliveryList= new ArrayList<>();

    public void addDelivery(Delivery delivery) {
        deliveryList.add(delivery);
    }

    public void removeDelivery(Delivery delivery) {
        deliveryList.remove(delivery);
    }


    public void step() {
        List<Delivery> deliveriesToRemove = new ArrayList<>();
        for (Delivery delivery : deliveryList) {
            delivery.step();
            if (delivery.isArrive()) {
                delivery.finishDelivery();
                deliveriesToRemove.add(delivery);
            }
        }
        for (Delivery delivery : deliveriesToRemove) {
            deliveryList.remove(delivery);
        }
    }

}
