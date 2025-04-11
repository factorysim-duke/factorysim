package edu.duke.ece651.factorysim;

import java.util.*;
/**
 * A utility class responsible for finding a path between two coordinates on a tile map.
 * The path must avoid obstacles, reuse existing roads when possible, and minimize a cost metric
 * based on path length and number of new tiles created.
 */
public class PathFinder {
    private static class Node {
        Coordinate coord;
        Node parent;
        int steps;
        int newTiles;

        public Node(Coordinate coord, Node parent, int steps, int newTiles) {
            this.coord = coord;
            this.parent = parent;
            this.steps = steps;
            this.newTiles = newTiles;
        }
        public int getCost(){
            return steps + newTiles;
        }
    }

    /**
     * Attempts to find a valid path between the source and destination coordinates on the given tile map.
     * The path is built to avoid buildings, reuse roads, and minimize the total cost (steps + new tiles).
     *
     * @param source the starting coordinate
     * @param destination the ending coordinate
     * @param tileMap the tile map with current layout of buildings and roads
     * @return a Path object representing the found path, or null if no path exists
     */
    public static Path findPath(Coordinate source, Coordinate destination, TileMap tileMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(Node::getCost));
        Set<Coordinate> visited = new HashSet<>();

        Node startNode = new Node(source, null, 0, 0);
        pq.add(startNode);

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.contains(current.coord)) {
                continue;
            }
            visited.add(current.coord);

            if (current.coord.equals(destination)) {
                return buildPath(current, tileMap);
            }

            for (int dir=0; dir<4; dir++) {
                Coordinate nextCoord = current.coord.getNextCoord(dir);

                // check bounds
                if (!tileMap.isInsideMap(nextCoord)) {
                    continue;
                }

                TileType nextTileType = tileMap.getTileType(nextCoord);
                if (nextTileType == TileType.BUILDING) {
                    // if the next tile is a building and not the destination, skip it
                    if(!nextCoord.equals(destination))
                    {
                        continue;
                    }
                }
                else
                {
                    // if current tile corresponding dir is already flow in, skip it
                    if (tileMap.getFlow(current.coord, dir) == -1) {
                        continue;
                    }
                }

                int nextNewTiles = current.newTiles + (nextTileType == TileType.ROAD ? 1 : 0);
                int nextSteps = current.steps + 1;

                Node nextNode = new Node(nextCoord, current, nextSteps, nextNewTiles);
                pq.add(nextNode);
            }
        }

        // No path found
        return null;
    }

    /**
     * Returns the direction code (0=up, 1=right, 2=down, 3=left) from one coordinate to another.
     *
     * @param from the starting coordinate
     * @param to the destination coordinate
     * @return the direction integer
     * @throws IllegalArgumentException if the coordinates are not adjacent
     */
    protected static int getDirection(Coordinate from, Coordinate to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        if (dx == 0 && dy == -1) return 0; // up
        if (dx == 1 && dy == 0) return 1; // right
        if (dx == 0 && dy == 1) return 2; // down
        if (dx == -1 && dy == 0) return 3; // left
        throw new IllegalArgumentException("Invalid move from " + from + " to " + to);
    }

    /**
     * Builds a Path object from the final node by tracing back through the parent links.
     *
     * @param endNode the final node of the path
     * @param tileMap the tile map used to check if a tile is newly built
     * @return a Path object representing the full route from source to destination
     */
    private static Path buildPath(Node endNode, TileMap tileMap) {
        List<Node> nodeList = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            nodeList.add(current);
            current = current.parent;
        }
        Collections.reverse(nodeList);

        Path path = new Path();
        for (int i = 0; i < nodeList.size(); ++i) {
            Node node = nodeList.get(i);
            Coordinate coord = node.coord;
            boolean isNew = (tileMap.getTileType(coord) == TileType.ROAD);

            int dir = -1;
            if (i + 1 < nodeList.size()) {
                Coordinate to = nodeList.get(i + 1).coord;
                dir = getDirection(coord, to);
            }

            path.emplaceBack(coord, isNew, dir);
        }

        return path;
    }


}