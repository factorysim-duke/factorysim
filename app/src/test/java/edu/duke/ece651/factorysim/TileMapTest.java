package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        map.setTileType(new Coordinate(4, 0), TileType.PATH);
        map.setTileType(new Coordinate(0, 1), TileType.BUILDING);

        String expectedOutput = "BRPPP\n" +
                "BRRRR\n" + "RRRRR\n" + "RRRRR\n" + "RRRRR\n";
        assertEquals(expectedOutput, map.toString());
    }
}