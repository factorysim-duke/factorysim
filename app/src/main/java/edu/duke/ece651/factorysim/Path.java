package edu.duke.ece651.factorysim;

import java.util.*;

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

    public void emplaceBack(Coordinate c, boolean isNew, int dir) {
        steps.add(c);
        flowDirections.add(dir);
        if (isNew) {
            newTiles.add(c);
        }
    }

    public List<Coordinate> getSteps() {
        return steps;
    }

    public Set<Coordinate> getNewTiles() {
        return newTiles;
    }

    public List<Integer> getFlowDirections() {
        return flowDirections;
    }

    public int getTotalLength() {
        return steps.size();
    }

    public int getNewTileCount() {
        return getNewTiles().size();
    }

    public int getCost() {
        return getTotalLength() + getNewTileCount();
    }

    public void dump() {
        System.out.println("Path from " + steps.get(0) + " to " + steps.get(steps.size()-1));
        System.out.println("Total steps: " + getTotalLength());
        System.out.println("New tiles: " + getNewTileCount());
        System.out.println("Steps:");
        for (Coordinate step : steps) {
            System.out.println(step);
        }
    }

    @Override
    public String toString() {
        return "Path from " + steps.get(0) + " to " + steps.get(steps.size()-1) +
                ", total steps=" + getTotalLength() +
                ", newTiles=" + getNewTileCount() +
                ", cost=" + getCost();
    }
}
