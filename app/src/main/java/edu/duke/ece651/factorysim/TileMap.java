package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * Represents the grid-based map used in the factory simulation.
 * It stores tile types (e.g., ROAD, PATH, BUILDING) and directional flow for each tile.
 * Also supports visual rendering, path addition, and JSON serialization.
 */
public class TileMap {

    private final Map<Coordinate, TileType> tileMap;
    // directions: 0 = up, 1 = right, 2 = down, 3 = left
    // flow: -1 = flow in, 0 = no flow, 1 = flow out, 2 = flow both ways
    // [-1, 0, 1, 0] means flow in from up, no flow from right, flow out to down, no flow from left
    private final Map<Coordinate, int[]> pathFlows;
    private final int width;
    private final int height;

    /** @return width of the tile map */
    public int getWidth() {
        return width;
    }

    /** @return height of the tile map */
    public int getHeight() {
        return height;
    }

    public TileMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tileMap = new HashMap<>();
        this.pathFlows = new HashMap<>();
        initializeTileMap();
    }

    /** Initializes the tile map with ROAD type tiles and no flow. */
    private void initializeTileMap() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tileMap.put(new Coordinate(x, y), TileType.ROAD);
                pathFlows.put(new Coordinate(x, y), new int[]{0, 0, 0, 0});
            }
        }
    }

    /**
     * Checks whether a coordinate lies within the map bounds.
     *
     * @param c the coordinate to check
     * @return true if inside map bounds, false otherwise
     */
    public boolean isInsideMap(Coordinate c) {
        return c.getX() >= 0 && c.getX() < width && c.getY() >= 0 && c.getY() < height;
    }

    /**
     * Returns the type of a tile at the given coordinate.
     *
     * @param c coordinate to check
     * @return tile type, or null if out of bounds
     */
    public TileType getTileType(Coordinate c) {
        return tileMap.getOrDefault(c, null);
    }

    /**
     * Sets the type of a tile at a given coordinate.
     *
     * @param c coordinate of the tile
     * @param type tile type to set
     * @throws IllegalArgumentException if coordinate is out of bounds
     */
    public void setTileType(Coordinate c, TileType type) {
        if (!isInsideMap(c)) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + c);
        }
        tileMap.put(c, type);
    }

    /**
     * Checks whether a tile is available for building a new path.
     *
     * @param c the coordinate to check
     * @return true if the tile is a ROAD, false otherwise
     */
    public boolean isAvailable(Coordinate c) {
        TileType type = getTileType(c);
        return type == TileType.ROAD;
    }

    @Override
    public String toString() {
        List<String> boardLines = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            StringBuilder[] tileLines = new StringBuilder[5];
            for (int i = 0; i < 5; i++) {
                tileLines[i] = new StringBuilder();
            }
            for (int x = 0; x < width; x++) {
                Coordinate c = new Coordinate(x, y);
                TileType type = getTileType(c);
                char center = ' ';
                if (type == TileType.BUILDING) {
                    center = 'B';
                } else if (type == TileType.PATH) {
                    center = 'P';
                }
                int[] flows = pathFlows.get(c);
                char arrowUp =      flows[0] == 2 ? '|' : (flows[0] == 1 ? '^' : (flows[0] == -1 ? 'v' : ' '));
                char arrowRight =   flows[1] == 2 ? '=' : (flows[1] == 1 ? '>' : (flows[1] == -1 ? '<' : ' '));
                char arrowDown =    flows[2] == 2 ? '|' : (flows[2] == 1 ? 'v' : (flows[2] == -1 ? '^' : ' '));
                char arrowLeft =    flows[3] == 2 ? '=' : (flows[3] == 1 ? '<' : (flows[3] == -1 ? '>' : ' '));

                String[] tileRep = new String[5];
                tileRep[0] = "+-----+";
                tileRep[1] = "|  " + arrowUp + "  |";
                tileRep[2] = "|" + arrowLeft + " " + center + " " + arrowRight + "|";
                tileRep[3] = "|  " + arrowDown + "  |";
                tileRep[4] = "+-----+";

                for (int i = 0; i < 5; i++) {
                    if (x > 0) {
                        tileLines[i].append(" ");
                    }
                    tileLines[i].append(tileRep[i]);
                }
            }
            for (int i = 0; i < 5; i++) {
                boardLines.add(tileLines[i].toString());
            }
        }

        int maxWidth = 0;
        for (String line : boardLines) {
            maxWidth = Math.max(maxWidth, line.length());
        }
        StringBuilder finalOutput = new StringBuilder();
        finalOutput.append("+" + "-".repeat(maxWidth) + "+\n");
        for (String line : boardLines) {
            finalOutput.append("|").append(line);
            finalOutput.append("|\n");
        }
        finalOutput.append("+" + "-".repeat(maxWidth) + "+");
        return finalOutput.toString();
    }

    /**
     * Retrieves the flow value for a given direction at a coordinate.
     *
     * @param coord the coordinate to inspect
     * @param directionIndex direction index (0=up, ..., 3=left)
     * @return flow value
     * @throws IllegalArgumentException if coordinate is out of bounds
     */
    public int getFlow(Coordinate coord, int directionIndex) {
        if (!isInsideMap(coord)) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coord);
        }
        return pathFlows.get(coord)[directionIndex];
    }

    /**
     * Sets the flow in a particular direction at a given coordinate.
     *
     * @param c the coordinate
     * @param dir direction (0=up, ..., 3=left)
     * @param flow flow value to set (-1, 0, 1, or 2)
     * @throws IllegalArgumentException if conflicting flow already exists
     */
    public void setFlow(Coordinate c, int dir, int flow) {
        int existing = getFlow(c, dir);
        if (existing != 0 && existing != flow) {
            throw new IllegalArgumentException("Flow conflict at " + c + " dir=" + dir +
                    ": existing=" + existing + ", new=" + flow);
        }
        if (existing == 0) {
            pathFlows.get(c)[dir] = flow;
        }
    }

    /**
     * Adds a path to the map by updating tile types and flow directions accordingly.
     * Reuses or extends roads with correct flow semantics.
     *
     * @param path the Path object representing the route to add
     * @throws IllegalArgumentException if step count and flow count mismatch
     */
    public void addPath(Path path) {
        List<Coordinate> steps = path.getSteps();
        List<Integer> dirs = path.getFlowDirections();

        if (steps.size() != dirs.size()) {
            throw new IllegalArgumentException("Mismatch between steps and flowDirections");
        }

        for (int i = 0; i < steps.size() - 1; i++) {
            Coordinate from = steps.get(i);
            int dir = dirs.get(i);

            Coordinate to = from.getNextCoord(dir);

            // allow start and end to flow both ways
            if (i == 0 || i == steps.size() - 2)
            {
                setFlow(from, dir, 2);

                int opp = (dir + 2) % 4;
                setFlow(to, opp, 2);
            }
            else
            {
                setFlow(from, dir, 1);

                int opp = (dir + 2) % 4;
                setFlow(to, opp, -1);
            }

            if (getTileType(from) != TileType.BUILDING) {
                setTileType(from, TileType.PATH);
            }
            if (getTileType(to) != TileType.BUILDING) {
                setTileType(to, TileType.PATH);
            }
        }
    }

    /**
     * Serializes the current tile map to a JSON object.
     * Includes width, height, tile types, and flow data.
     *
     * @return a JsonObject representing the map
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("width", width);
        json.addProperty("height", height);

        JsonArray tileArr=new JsonArray();
        for(Map.Entry<Coordinate,TileType>entry:tileMap.entrySet()) {
            Coordinate c= entry.getKey();
            JsonObject tileObj=new JsonObject();
            tileObj.addProperty("x", c.getX());
            tileObj.addProperty("y",c.getY());
            tileObj.addProperty("type", entry.getValue().toString());
            int[]flow=pathFlows.get(c);
            JsonArray flowArr=new JsonArray();
            for (int j : flow) {
                flowArr.add(j);
            }
            tileObj.add("flow",flowArr);
            tileArr.add(tileObj);
        }
        json.add("tiles", tileArr);
        return json;
    }
}
