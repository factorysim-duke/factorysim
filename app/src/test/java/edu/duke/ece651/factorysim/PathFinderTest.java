package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathFinderTest {
    TileMap tileMap = new TileMap(10, 10);
    Coordinate m1 = new Coordinate(0, 2);
    Coordinate m2 = new Coordinate(0, 4);
    Coordinate m3 = new Coordinate(0, 6);
    Coordinate m4 = new Coordinate(0, 8);
    Coordinate f1 = new Coordinate(5, 5);
    Coordinate f2 = new Coordinate(6, 5);

    @Test
    void test_findPath() {
        tileMap.setTileType(m1,TileType.BUILDING);
        tileMap.setTileType(m2,TileType.BUILDING);
        tileMap.setTileType(m3,TileType.BUILDING);
        tileMap.setTileType(m4,TileType.BUILDING);
        tileMap.setTileType(f1,TileType.BUILDING);
        tileMap.setTileType(f2,TileType.BUILDING);

        System.out.println("TileMap:\n" + tileMap);

        Path path = PathFinder.findPath(m1, f1, tileMap);
        System.out.println("Path:\n" + path);
        assertNotNull(path, "Should find a path");
        assertEquals(m1, path.getSteps().get(0), "Path start point should be m1");
        assertEquals(f1, path.getSteps().get(path.getSteps().size() - 1), "Path end point should be f1");
        assertEquals(9, path.getTotalLength(), "Path steps length should be 9");
        assertEquals(7, path.getNewTileCount(), "Path should have 7 new tiles");
    }

    @Test
    void test_findPath_impossible() {
        TileMap tileMap = new TileMap(10, 10);
        Coordinate source = new Coordinate(0, 0);
        Coordinate dest = new Coordinate(3, 0);

        tileMap.setTileType(source, TileType.BUILDING);
        tileMap.setTileType(dest, TileType.BUILDING);
        tileMap.setTileType(new Coordinate(2, 0), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(2, 1), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(0, 2), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(1, 2), TileType.BUILDING);


        Path path = PathFinder.findPath(source, dest, tileMap);
        assertNull(path, "Path should be null for impossible case");
    }

    @Test
    void test_getDirection_valid() {
        Coordinate from = new Coordinate(5, 5);

        assertEquals(1, PathFinder.getDirection(from, new Coordinate(6, 5)), "Right should be direction 1");
        assertEquals(2, PathFinder.getDirection(from, new Coordinate(5, 6)), "Down should be direction 2");
        assertEquals(3, PathFinder.getDirection(from, new Coordinate(4, 5)), "Left should be direction 3");
        assertEquals(0, PathFinder.getDirection(from, new Coordinate(5, 4)), "Up should be direction 0");
    }

    @Test
    void test_getDirection_invalid() {
        Coordinate from = new Coordinate(5, 5);
        Coordinate to = new Coordinate(5, 5); // Invalid move
        Coordinate to2 = new Coordinate(6, 6);
        Coordinate to3 = new Coordinate(4, 4);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PathFinder.getDirection(from, to);
        });
        assertTrue(exception.getMessage().contains("Invalid move"), "Should throw exception for invalid direction");

        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            PathFinder.getDirection(from, to2);
        });
        assertTrue(exception2.getMessage().contains("Invalid move"), "Should throw exception for invalid direction");

        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            PathFinder.getDirection(from, to3);
        });
        assertTrue(exception3.getMessage().contains("Invalid move"), "Should throw exception for invalid direction");
    }

    @Test
    public void test_findPath_skip_due_to_flow_direction() {
        TileMap customTileMap = new TileMap(10, 10) {
            @Override
            public int getFlow(Coordinate coord, int dir) {
                if (coord.equals(new Coordinate(0, 0)) && dir == 1) {
                    return -1;
                }
                return 0;
            }
        };

        customTileMap.setTileType(new Coordinate(0, 2), TileType.BUILDING);

        Coordinate source = new Coordinate(0, 0);
        Coordinate destination = new Coordinate(0, 2);

        Path path = PathFinder.findPath(source, destination, customTileMap);

        assertNotNull(path, "Should find a path");
        List<Coordinate> steps = path.getSteps();
        assertEquals(3, steps.size(), "Path should have 3 steps");
        assertEquals(new Coordinate(0, 0), steps.get(0), "Start point should be (0,0)");
        assertEquals(new Coordinate(0, 1), steps.get(1), "Middle point should be (0,1)");
        assertEquals(new Coordinate(0, 2), steps.get(2), "End point should be (0,2)");
    }

    @Test
    void test_findPath_cost_update_with_path() {
        TileMap tileMap = new TileMap(5, 2);
        Coordinate source = new Coordinate(0, 0);
        Coordinate dest = new Coordinate(4, 0);

        // Set tile types
        tileMap.setTileType(source, TileType.BUILDING);
        tileMap.setTileType(dest, TileType.BUILDING);
        tileMap.setTileType(new Coordinate(0, 1), TileType.PATH);
        tileMap.setTileType(new Coordinate(1, 1), TileType.PATH);
        tileMap.setTileType(new Coordinate(2, 1), TileType.PATH);
        tileMap.setTileType(new Coordinate(3, 1), TileType.PATH);
        tileMap.setTileType(new Coordinate(4, 1), TileType.PATH);

        Path path = PathFinder.findPath(source, dest, tileMap);
        assertNotNull(path, "Should find a path");
//        path.dump();
        assertEquals(source, path.getSteps().get(0), "Start point should be (0,0)");
        assertEquals(dest, path.getSteps().get(path.getSteps().size() - 1), "End point should be (0,3)");
    }

    class CustomTileMap extends TileMap {
        public CustomTileMap(int width, int height) {
            super(width, height);
        }

        @Override
        public int getFlow(Coordinate coord, int dir) {
            if (coord.equals(new Coordinate(4, 3)) && dir == 1) {
                return -1;
            }
            return 0;
        }
    }

    @Test
    public void test_findPath_complex_scenario() {
        CustomTileMap tileMap = new CustomTileMap(8, 8);
        Coordinate start = new Coordinate(2, 3);
        Coordinate end = new Coordinate(5, 5);

        tileMap.setTileType(start, TileType.BUILDING);
        tileMap.setTileType(end, TileType.BUILDING);
        tileMap.setTileType(new Coordinate(6, 2), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(7, 7), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(3, 6), TileType.BUILDING);
        tileMap.setTileType(new Coordinate(4, 2), TileType.BUILDING);

        tileMap.setTileType(new Coordinate(4, 3), TileType.PATH);

        Path path = PathFinder.findPath(start, end, tileMap);

        assertNotNull(path, "Should find a path");
//        path.dump();

        List<Coordinate> steps = path.getSteps();
        assertEquals(start, steps.get(0), "Should start at (2,3)");
        assertEquals(end, steps.get(steps.size() - 1), "Should end at (5,5)");

        for (int i = 0; i < steps.size() - 1; i++) {
            Coordinate from = steps.get(i);
            Coordinate to = steps.get(i + 1);
            int dir = PathFinder.getDirection(from, to);
            if (i != 0 && i != steps.size() - 2) {
                assertNotEquals(-1, tileMap.getFlow(from, dir), "Flow direction should be -1 for middle steps");
            }
        }

        int expectedNewTileCount = 0;
        for (Coordinate coord : steps) {
            if (tileMap.getTileType(coord) == TileType.ROAD) {
                expectedNewTileCount++;
            }
        }
        assertEquals(expectedNewTileCount, path.getNewTileCount(), "New tile count should match expected value");
    }

    @Test
    public void test_findPath_both_ways() {
        TileMap tileMap = new TileMap(5, 5);
        System.out.println(tileMap);

        Coordinate p1 = new Coordinate(0, 0);
        Coordinate p2 = new Coordinate(1, 0);
        Coordinate p3 = new Coordinate(2, 0);
        Coordinate p4 = new Coordinate(3, 0);
        Coordinate p5 = new Coordinate(4, 0);

        tileMap.setTileType(p1, TileType.BUILDING);
        tileMap.setTileType(p2, TileType.BUILDING);
        tileMap.setTileType(p3, TileType.BUILDING);
        tileMap.setTileType(p4, TileType.BUILDING);
        tileMap.setTileType(p5, TileType.BUILDING);

        Path path = PathFinder.findPath(p1, p3, tileMap);

        // add path
        assertNotNull(path, "Should find a path");
        tileMap.addPath(path);

        path = PathFinder.findPath(p3, p5, tileMap);

        // add path
        assertNotNull(path, "Should find a path");
        tileMap.addPath(path);

        System.out.println(tileMap);

        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p2));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p3));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p4));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p5));
        assertEquals(TileType.PATH, tileMap.getTileType(new Coordinate(0,1)));
        assertEquals(TileType.PATH, tileMap.getTileType(new Coordinate(1,1)));
        assertEquals(TileType.PATH, tileMap.getTileType(new Coordinate(2,1)));
        assertEquals(TileType.PATH, tileMap.getTileType(new Coordinate(3,1)));
        assertEquals(TileType.PATH, tileMap.getTileType(new Coordinate(4,1)));

        assertEquals(2, tileMap.getFlow(p1, 2));
        assertEquals(2, tileMap.getFlow(p3, 2));
        assertEquals(2, tileMap.getFlow(p5, 2));
        assertEquals(2, tileMap.getFlow(new Coordinate(0,1), 0));
        assertEquals(1, tileMap.getFlow(new Coordinate(0,1), 1));
        assertEquals(-1, tileMap.getFlow(new Coordinate(1,1), 3));
        assertEquals(1, tileMap.getFlow(new Coordinate(1,1), 1));
        assertEquals(-1, tileMap.getFlow(new Coordinate(2,1), 3));
        assertEquals(2, tileMap.getFlow(new Coordinate(2,1), 0));
        assertEquals(1, tileMap.getFlow(new Coordinate(2,1), 1));
        assertEquals(-1, tileMap.getFlow(new Coordinate(3,1), 3));
        assertEquals(1, tileMap.getFlow(new Coordinate(3,1), 1));
        assertEquals(-1, tileMap.getFlow(new Coordinate(4,1), 3));
        assertEquals(2, tileMap.getFlow(new Coordinate(4,1), 0));


    }
}