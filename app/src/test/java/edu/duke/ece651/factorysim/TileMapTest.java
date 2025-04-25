package edu.duke.ece651.factorysim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TileMapTest {

    @Test
    public void test_initialization() {
        TileMap map = new TileMap(25, 25);

        for (int x = 0; x < 25; x++) {
            for (int y = 0; y < 25; y++) {
                Coordinate c = new Coordinate(x, y);
                assertEquals(TileType.ROAD, map.getTileType(c));
                assertTrue(map.isInsideMap(c));
                assertTrue(map.isAvailable(c));
            }
        }

        assertFalse(map.isInsideMap(new Coordinate(25, 0)));
        assertFalse(map.isInsideMap(new Coordinate(0, 25)));
        assertFalse(map.isInsideMap(new Coordinate(-1, 5)));
    }

    // getFlow
    @Test
    public void test_get_flow_out_of_bounds() {
        TileMap map = new TileMap(25, 25);
        Coordinate out = new Coordinate(30, 30);
        assertThrows(IllegalArgumentException.class, () -> map.getFlow(out, 0));
    }

    @Test
    public void test_set_flow_valid_already_exists() {
        TileMap map = new TileMap(25, 25);
        Coordinate coord = new Coordinate(10, 10);
        map.setFlow(coord, 0, 1);
        map.setFlow(coord, 0, 1);
        assertEquals(1, map.getFlow(coord, 0));
    }

    @Test
    public void test_is_inside_map() {
        TileMap map = new TileMap(25, 25);

        assertTrue(map.isInsideMap(new Coordinate(0, 0)), "(0,0) should be inside the map");
        assertTrue(map.isInsideMap(new Coordinate(24, 24)), "(24,24) should be inside the map");
        assertTrue(map.isInsideMap(new Coordinate(10, 10)), "(10,10) should be inside the map");

        assertFalse(map.isInsideMap(new Coordinate(-1, 0)), "(-1,0) should be outside the map");
        assertFalse(map.isInsideMap(new Coordinate(0, -1)), "(0,-1) should be outside the map");
        assertFalse(map.isInsideMap(new Coordinate(-5, -5)), "(-5,-5) should be outside the map");

        assertFalse(map.isInsideMap(new Coordinate(25, 10)), "(25,10) should be outside the map");
        assertFalse(map.isInsideMap(new Coordinate(10, 25)), "(10,25) should be outside the map");
        assertFalse(map.isInsideMap(new Coordinate(26, 30)), "(26,30) should be outside the map");
    }

    @Test
    public void test_set_and_get_tile_type() {
        TileMap map = new TileMap(25, 25);
        Coordinate coord = new Coordinate(10, 10);

        map.setTileType(coord, TileType.BUILDING);
        assertEquals(TileType.BUILDING, map.getTileType(coord));
        assertFalse(map.isAvailable(coord));

        map.setTileType(coord, TileType.PATH);
        assertEquals(TileType.PATH, map.getTileType(coord));
        assertFalse(map.isAvailable(coord));
    }

    @Test
    public void test_set_tile_out_of_bounds_should_throw() {
        TileMap map = new TileMap(25, 25);
        Coordinate out = new Coordinate(30, 30);
        assertThrows(IllegalArgumentException.class, () -> map.setTileType(out, TileType.BUILDING));
    }

    @Test
    public void test_toString() {
        TileMap map = new TileMap(5, 5);

        map.setTileType(new Coordinate(0, 0), TileType.BUILDING);
        map.setTileType(new Coordinate(2, 0), TileType.PATH);
        map.setTileType(new Coordinate(3, 0), TileType.PATH);
        map.setTileType(new Coordinate(0, 1), TileType.BUILDING);

        map.setFlow(new Coordinate(2,0),0,-1);
        map.setFlow(new Coordinate(2,0),1,-1);
        map.setFlow(new Coordinate(2,0),3,-1);
        map.setFlow(new Coordinate(2,0),2,-1);

        map.setFlow(new Coordinate(3,0),0,1);
        map.setFlow(new Coordinate(3,0),1,1);
        map.setFlow(new Coordinate(3,0),2,1);
        map.setFlow(new Coordinate(3,0),3,1);

        String expectedOutput = "+---------------------------------------+\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "||     | |     | |  v  | |  ^  | |     ||\n" +
                "||  B  | |     | |> P <| |< P >| |     ||\n" +
                "||     | |     | |  ^  | |  v  | |     ||\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||  B  | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "||     | |     | |     | |     | |     ||\n" +
                "|+-----+ +-----+ +-----+ +-----+ +-----+|\n" +
                "+---------------------------------------+";
        assertEquals(expectedOutput, map.toString());
    }

    class DummyPath extends Path {
        private List<Coordinate> steps;
        private List<Integer> flowDirections;

        public DummyPath(List<Coordinate> steps, List<Integer> flowDirections) {
            this.steps = steps;
            this.flowDirections = flowDirections;
        }

        @Override
        public List<Coordinate> getSteps() {
            return steps;
        }

        @Override
        public List<Integer> getFlowDirections() {
            return flowDirections;
        }
    }

    @Test
    public void test_addPath_valid() {
        TileMap tileMap = new TileMap(5, 5);

        Coordinate p1 = new Coordinate(1, 1);
        Coordinate p2 = new Coordinate(1, 2);
        Coordinate p3 = new Coordinate(1, 3);
        Coordinate p4 = new Coordinate(1, 4);

        tileMap.setTileType(p1, TileType.BUILDING);
        tileMap.setTileType(p4, TileType.BUILDING);

        DummyPath path = new DummyPath(
                Arrays.asList(p1, p2, p3, p4),
                Arrays.asList(2, 2, 2, -1)
        );

        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.ROAD, tileMap.getTileType(p2));
        assertEquals(TileType.ROAD, tileMap.getTileType(p3));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p4));

        // add path
        tileMap.addPath(path);

        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.PATH, tileMap.getTileType(p2));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.PATH, tileMap.getTileType(p2));
        assertEquals(TileType.PATH, tileMap.getTileType(p3));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p4));

        assertEquals(2, tileMap.getFlow(p1, 2));
        assertEquals(2, tileMap.getFlow(p2, 0));
        assertEquals(1, tileMap.getFlow(p2, 2));
        assertEquals(-1, tileMap.getFlow(p3, 0));
        assertEquals(2, tileMap.getFlow(p3, 2));
        assertEquals(2, tileMap.getFlow(p4, 0));
    }

    @Test
    public void test_addPath_valid_close() {
        TileMap tileMap = new TileMap(5, 5);

        Coordinate p1 = new Coordinate(1, 1);
        Coordinate p4 = new Coordinate(1, 2);

        tileMap.setTileType(p1, TileType.BUILDING);
        tileMap.setTileType(p4, TileType.BUILDING);

        DummyPath path = new DummyPath(
                Arrays.asList(p1, p4),
                Arrays.asList(2, -1)
        );

        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p4));

        // add path
        tileMap.addPath(path);

        assertEquals(TileType.BUILDING, tileMap.getTileType(p1));
        assertEquals(TileType.BUILDING, tileMap.getTileType(p4));

        assertEquals(2, tileMap.getFlow(p1, 2));
        assertEquals(2, tileMap.getFlow(p4, 0));
    }

    @Test
    public void test_addPath_mismatchedStepsAndDirections() {
        TileMap tileMap = new TileMap(5, 5);
        Coordinate p1 = new Coordinate(1, 1);
        Coordinate p2 = new Coordinate(1, 2);

        DummyPath path = new DummyPath(
                Arrays.asList(p1, p2),
                Arrays.asList(2)
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tileMap.addPath(path);
        });
        String expectedMessage = "Mismatch between steps and flowDirections";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void test_addPath_flowConflict() {
        TileMap tileMap = new TileMap(5, 5);
        Coordinate p1 = new Coordinate(2, 2);
        Coordinate p2 = new Coordinate(2, 3);

        DummyPath validPath = new DummyPath(
                Arrays.asList(p1, p2),
                Arrays.asList(2, -1)
        );

        tileMap.setFlow(p1, 2, -1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tileMap.addPath(validPath);
        });
        String expectedMessage = "Flow conflict";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testToJson() {
        TileMap tm = new TileMap(2, 2);
        JsonObject json = tm.toJson();

        // test the width and height fields.
        assertTrue(json.has("width"));
        assertTrue(json.has("height"));
        assertEquals(2, json.get("width").getAsInt());
        assertEquals(2, json.get("height").getAsInt());

        // Check the "tiles" array.
        assertTrue(json.has("tiles"));
        JsonArray tiles = json.getAsJsonArray("tiles");
        assertEquals(4, tiles.size());

        Set<String> foundCoords = new HashSet<>();

        // check each tile object.
        for (JsonElement element : tiles) {
            assertTrue(element.isJsonObject(), "Each tile should be a JsonObject");
            JsonObject tileObj = element.getAsJsonObject();

            assertTrue(tileObj.has("x"));
            assertTrue(tileObj.has("y"));
            assertTrue(tileObj.has("type"));
            assertTrue(tileObj.has("flow"));

            int x = tileObj.get("x").getAsInt();
            int y = tileObj.get("y").getAsInt();
            foundCoords.add(x + "," + y);

            // test road by default
            String type = tileObj.get("type").getAsString();
            assertEquals("ROAD", type);

            // test flow array are 0s.
            JsonArray flowArr = tileObj.getAsJsonArray("flow");
            assertEquals(4, flowArr.size());
            for (int i = 0; i < 4; i++) {
                assertEquals(0, flowArr.get(i).getAsInt());
            }
        }

        Set<String> expectedCoords = new HashSet<>(Arrays.asList("0,0", "0,1", "1,0", "1,1"));
        assertEquals(expectedCoords, foundCoords);
    }

    @Test
    public void test_get_flows_out_of_bounds() {
        TileMap map = new TileMap(25, 25);
        Coordinate out = new Coordinate(30, 30);
        assertThrows(IllegalArgumentException.class, () -> map.getFlows(out));
    }

}