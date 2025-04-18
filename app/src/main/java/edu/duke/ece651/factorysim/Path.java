package edu.duke.ece651.factorysim;

import com.google.gson.*;

import java.util.*;

/**
 * Represents a path in the simulation world connecting two buildings via a list of coordinates (tiles).
 * Tracks flow directions, new tiles placed, and provides path-related metrics.
 */
public class Path {
    private final List<Coordinate> steps;
    private final Set<Coordinate> newTiles;
    // directions: 0 = up, 1 = right, 2 = down, 3 = left
    private final List<Integer> flowDirections;

    public Path() {
        this.steps = new ArrayList<>();
        this.newTiles = new HashSet<>();
        this.flowDirections = new ArrayList<>();
    }
//    public Path(List<Coordinate> steps, Set<Coordinate> newTiles, List<Integer> flowDirections) {
//        this.steps = steps;
//        this.newTiles = newTiles;
//        this.flowDirections = flowDirections;
//    }

    /**
     * Adds a new coordinate step to the end of the path.
     *
     * @param c the coordinate to add
     * @param isNew whether this coordinate represents a newly built tile
     * @param dir the direction of flow into this tile
     */
    public void emplaceBack(Coordinate c, boolean isNew, int dir) {
        steps.add(c);
        flowDirections.add(dir);
        if (isNew) {
            newTiles.add(c);
        }
    }

    /**
     * @return list of coordinates representing the full path
     */
    public List<Coordinate> getSteps() {
        return steps;
    }

    /**
     * @return set of coordinates that are newly created tiles in this path
     */
    public Set<Coordinate> getNewTiles() {
        return newTiles;
    }

    /**
     * @return list of directions used along the path
     */
    public List<Integer> getFlowDirections() {
        return flowDirections;
    }

    /**
     * @return total number of steps (tiles) in the path
     */
    public int getTotalLength() {
        return steps.size();
    }

    /**
     * @return total number of new tiles in the path
     */
    public int getNewTileCount() {
        return getNewTiles().size();
    }

    /**
     * @return total cost of the path, calculated as the sum of total length and new tile count
     */
    public int getCost() {
        return getTotalLength() + getNewTileCount();
    }

    /**
     * @return the delivery time, which is the total length minus 2
     */
    public int getDeliveryTime(){
        return getTotalLength()-2;
    }

//    /**
//     * Prints debugging information about the path to the provided logger.
//     */
//    public void dump() {
//            System.out.println("Path from " + steps.get(0) + " to " + steps.get(steps.size()-1));
//            System.out.println("Total steps: " + getTotalLength());
//            System.out.println("New tiles: " + getNewTileCount());
//            System.out.println("Steps:");
//            for (Coordinate step : steps) {
//                System.out.println(step);
//            }
//    }


    /**
     * Checks if the path starts and ends at the specified coordinates.
     *
     * @param fromCoord the expected starting coordinate
     * @param toCoord the expected ending coordinate
     * @return true if the path starts at fromCoord and ends at toCoord
     */
    public boolean isMatch(Coordinate fromCoord, Coordinate toCoord) {
        return steps.getFirst().equals(fromCoord) && steps.getLast().equals(toCoord);
    }

    @Override
    public String toString() {
        return "Path from " + steps.get(0) + " to " + steps.get(steps.size()-1) +
                ", total steps=" + getTotalLength() +
                ", newTiles=" + getNewTileCount() +
                ", cost=" + getCost();
    }

//    /**
//     * Converts the path to a JSON object containing:
//     * - steps: list of coordinates
//     * - newTiles: set of newly built tiles
//     * - flowDirections: list of flow directions between tiles
//     *
//     * @return a JsonObject representing the path
//     */
//    public JsonObject toJson() {
//        JsonObject json = new JsonObject();
//
//        // Add steps array
//        JsonArray stepsArray = new JsonArray();
//        for (Coordinate c : steps) {
//            JsonObject coordJson = new JsonObject();
//            coordJson.addProperty("x", c.getX());
//            coordJson.addProperty("y", c.getY());
//            stepsArray.add(coordJson);
//        }
//        json.add("steps", stepsArray);
//
//        // Add newTiles array
//        JsonArray newTilesArray = new JsonArray();
//        for (Coordinate c : newTiles) {
//            JsonObject coordJson = new JsonObject();
//            coordJson.addProperty("x", c.getX());
//            coordJson.addProperty("y", c.getY());
//            newTilesArray.add(coordJson);
//        }
//        json.add("newTiles", newTilesArray);
//
//        // Add flowDirections array
//        JsonArray flowDirArray = new JsonArray();
//        for (Integer dir : flowDirections) {
//            flowDirArray.add(dir);
//        }
//        json.add("flowDirections", flowDirArray);
//        return json;
//    }
}

