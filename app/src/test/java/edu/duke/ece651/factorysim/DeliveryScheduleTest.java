package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
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
        sim.connectBuildings("W", "D");
        Delivery delivery = new Delivery(W, D, metal, 1, 5);
        Delivery d2 = new Delivery(W, D, wood, 1, 5, 0, 0, W.getLocation());
        deliverySchedule.addDelivery(delivery);
        deliverySchedule.addDelivery(d2);
        assertEquals(W.getLocation(), deliverySchedule.getCurrentCoordinates().get(0));
        deliverySchedule.step(sim.getPathList());
        assertEquals(4, delivery.deliveryTime);
        assertEquals(4, d2.deliveryTime);
        assertEquals(2, deliverySchedule.getCurrentCoordinates().size());
        assertEquals(sim.getPathList().get(0).getSteps().get(1), deliverySchedule.getCurrentCoordinates().get(0));
    }
    @Test
    void test_toJson(){
        Delivery delivery=new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        JsonArray json=deliverySchedule.toJson();
        assertEquals(1,json.size());
        assertEquals("metal",json.get(0).getAsJsonObject().get("item").getAsString());
        assertEquals(1,json.get(0).getAsJsonObject().get("quantity").getAsInt());
        assertEquals(5,json.get(0).getAsJsonObject().get("deliveryTime").getAsInt());
    }

    @Test
    void test_subscribe_unsubscribe() {
        class TestListener implements DeliveryListener {
            public boolean added = false;
            public boolean finished = false;

            @Override
            public void onDeliveryAdded(Delivery delivery) {
                added = true;
            }

            @Override
            public void onDeliveryFinished(Delivery delivery) {
                finished = true;
            }
        }
        TestListener listener = new TestListener();
        assertFalse(listener.added);
        assertFalse(listener.finished);

        deliverySchedule.subscribe(listener);

        Delivery delivery = new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        assertTrue(listener.added);

        sim.connectBuildings("W", "D");
        for (int i = 0; i < 5; i++) {
            deliverySchedule.step(sim.getPathList());
        }
        assertTrue(listener.finished);

        deliverySchedule.unsubscribe(listener);
    }

    @Test
    void test_checkUsingPath() {
        sim.connectBuildings("W", "D");
        Path path = sim.getPathList().get(0);
        Delivery delivery = new Delivery(W, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery);
        assertTrue(deliverySchedule.checkUsingPath(path));
        deliverySchedule.removeDelivery(delivery);
        assertFalse(deliverySchedule.checkUsingPath(path));
        sim.connectBuildings("Hi", "D");
        Delivery delivery2= new Delivery(Hi, D, metal, 1, 5);
        deliverySchedule.addDelivery(delivery2);
        deliverySchedule.step(sim.getPathList());
        deliverySchedule.step(sim.getPathList());
        assertTrue(deliverySchedule.checkUsingPath(sim.getPathList().get(1)));

    }
}