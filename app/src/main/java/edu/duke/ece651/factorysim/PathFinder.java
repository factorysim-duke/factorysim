package edu.duke.ece651.factorysim;

import java.util.*;

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

    protected static int getDirection(Coordinate from, Coordinate to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        if (dx == 0 && dy == -1) return 0; // up
        if (dx == 1 && dy == 0) return 1; // right
        if (dx == 0 && dy == 1) return 2; // down
        if (dx == -1 && dy == 0) return 3; // left
        throw new IllegalArgumentException("Invalid move from " + from + " to " + to);
    }

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