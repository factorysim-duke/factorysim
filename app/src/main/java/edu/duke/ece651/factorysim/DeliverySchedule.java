package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a schedule of deliveries in the simulation.
 * Manages a list of deliveries and updates them over simulation time.
 */
public class DeliverySchedule {
    List<Delivery> deliveryList= new ArrayList<>();

    /**
     * Adds a delivery to the schedule.
     *
     * @param delivery the Delivery to be added
     */
    public void addDelivery(Delivery delivery) {
        deliveryList.add(delivery);
    }

    /**
     * Removes a delivery from the schedule.
     *
     * @param delivery the Delivery to be removed
     */
    public void removeDelivery(Delivery delivery) {
        deliveryList.remove(delivery);
    }

    /**
     * Advances the simulation by one timestep.
     * Each delivery is updated (its delivery time is decreased), and if any delivery arrives,
     * it is processed and removed from the schedule.
     */
    public void step(List<Path> pathList) {
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
        for (Delivery delivery : deliveryList) {
            delivery.updateCurrentCoordinate(pathList);
        }
    }

    public List<Coordinate> getCurrentCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (Delivery delivery : deliveryList) {
            coordinates.add(delivery.getCurrentCoordinate());
        }
        return coordinates;
    }

    /**
     * Checks if any delivery in the schedule is using a specific path.
     *
     * @param pathIndex the index of the path to check
     * @return true if any delivery is using the specified path, false otherwise
     */
    public boolean checkUsingPath(int pathIndex) {
        for (Delivery delivery : deliveryList) {
            if (delivery.isUsingPath(pathIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts all current deliveries in the schedule to a JSON array.
     *
     * @return a JsonArray representing the list of deliveries in the schedule
     */
    public JsonArray toJson(){
        JsonArray json = new JsonArray();
        for (Delivery delivery : deliveryList) {
            json.add(delivery.toJson());
        }
        return json;
    }
}
