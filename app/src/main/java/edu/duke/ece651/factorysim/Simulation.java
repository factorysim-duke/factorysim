
package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Runs the factory simulation, managing buildings and item production.
 */
public class Simulation {
    private final World world;
    private int currentTime;
    private boolean finished = false;

    /**
     * Creates a simulation from a JSON configuration file.
     *
     * @param jsonFilePath the path to the JSON file.
     */
    public Simulation(String jsonFilePath) {
        this.currentTime = 0;
        ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
        this.world = WorldBuilder.buildWorld(configData);
    }

    /**
     * Working on the simulation for n steps.
     *
     * @param n the number of steps.
     * @throws IllegalArgumentException if n is less than 1 or >= Integer.MAX_VALUE.
     */
    public void step(int n) {
        if (n < 1 || n == Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < n; i++) {
            currentTime++;
            for (Building building : world.getBuildings()) {
                building.step();
            }
        }
    }

    /**
     * Requests a building to produce an item.
     *
     * @param itemName     the name of the item to produce.
     * @param buildingName the name of the building to produce the item.
     * @throws IllegalArgumentException if the building cannot produce the item or the building is not found.
     */
    public void request(String itemName, String buildingName) {
        for (Building building : world.getBuildings()) {
            if (building.getName().equals(buildingName)) {

                Item targetOutput = new Item(itemName);
                if (!building.canProduce(targetOutput)) {
                    throw new IllegalArgumentException("Building cannot produce item");
                }
                building.addRequest(new Request(itemName, building, true));
                return;
            }
        }
        throw new IllegalArgumentException("Building not found");
    }

    /**
     * Runs the simulation until all requests are completed, then exits.
     */
    public void finish() {
        while (!allRequestsFinished()) {
            step(1);
        }
        System.out.println("Simulation completed at time-step " + currentTime);
        finished = true;
    }

    /**
     * Checks if all building completed all their request.
     *
     * @return true if all buildings are done, false otherwise.
     */
    public boolean allRequestsFinished() {
        for (Building building : world.getBuildings()) {
            if (!building.isFinished()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the simulation is finished.
     *
     * @return true if finished, false otherwise.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Gets the current time step.
     *
     * @return the current time.
     */
    public int getCurrentTime() {
        return currentTime;
    }
}

