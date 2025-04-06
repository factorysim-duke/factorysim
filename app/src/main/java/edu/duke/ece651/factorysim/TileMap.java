package edu.duke.ece651.factorysim;

import java.util.*;

public class TileMap {

    private final Map<Coordinate, TileType> tileMap;
    // directions: 0 = up, 1 = right, 2 = down, 3 = left
    // flow: -1 = flow in, 0 = no flow, 1 = flow out
    // [-1, 0, 1, 0] means flow in from up, no flow from right, flow out to down, no flow from left
    private final Map<Coordinate, int[]> pathFlows;
    private final int width;
    private final int height;

    public int getWidth() {
        return width;
    }

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

    private void initializeTileMap() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tileMap.put(new Coordinate(x, y), TileType.ROAD);
                pathFlows.put(new Coordinate(x, y), new int[]{0, 0, 0, 0});
            }
        }
    }

    public boolean isInsideMap(Coordinate c) {
        return c.getX() >= 0 && c.getX() < width && c.getY() >= 0 && c.getY() < height;
    }

    public TileType getTileType(Coordinate c) {
        return tileMap.getOrDefault(c, null);
    }

    public void setTileType(Coordinate c, TileType type) {
        if (!isInsideMap(c)) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + c);
        }
        tileMap.put(c, type);
    }

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
                char arrowUp = flows[0] == 1 ? '^' : (flows[0] == -1 ? 'v' : ' ');
                char arrowRight = flows[1] == 1 ? '>' : (flows[1] == -1 ? '<' : ' ');
                char arrowDown = flows[2] == 1 ? 'v' : (flows[2] == -1 ? '^' : ' ');
                char arrowLeft = flows[3] == 1 ? '<' : (flows[3] == -1 ? '>' : ' ');

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

    public int getFlow(Coordinate coord, int directionIndex) {
        if (!isInsideMap(coord)) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coord);
        }
        return pathFlows.get(coord)[directionIndex];
    }

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

            setFlow(from, dir, 1);

            int opp = (dir + 2) % 4;
            setFlow(to, opp, -1);

            if (getTileType(from) != TileType.BUILDING) {
                setTileType(from, TileType.PATH);
            }
            if (getTileType(to) != TileType.BUILDING) {
                setTileType(to, TileType.PATH);
            }
        }
    }

}
