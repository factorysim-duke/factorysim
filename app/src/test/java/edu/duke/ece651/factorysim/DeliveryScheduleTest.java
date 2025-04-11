package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryScheduleTest {
    Simulation sim=new Simulation("src/test/resources/inputs/doors1.json");
    DeliverySchedule deliverySchedule=new DeliverySchedule();
    Item metal=new Item("metal");
    Item wood=new Item("wood");
    Building W=sim.getWorld().getBuildingFromName("W");
    Building D=sim.getWorld().getBuildingFromName("D");
    Building M=sim.getWorld().getBuildingFromName("M");
    Building Hi=sim.getWorld().getBuildingFromName("Hi");

    @Test
    void addDelivery() {
        Delivery delivery=new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        assertEquals(1, deliverySchedule.deliveryList.size());
        assertEquals(delivery, deliverySchedule.deliveryList.get(0));
    }

    @Test
    void removeDelivery() {
        Delivery delivery=new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        assertEquals(1, deliverySchedule.deliveryList.size());
        deliverySchedule.removeDelivery(delivery);
        assertEquals(0, deliverySchedule.deliveryList.size());
    }

    @Test
    void step() {
        Delivery delivery=new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        deliverySchedule.step();
        assertEquals(4, delivery.deliveryTime);
    }
}