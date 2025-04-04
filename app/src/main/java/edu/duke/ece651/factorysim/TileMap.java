package edu.duke.ece651.factorysim;

import java.util.*;

public class TileMap {
  private final Map<Coordinate, TileType> tileMap;
  private final int width;
  private final int height;

  public TileMap(int width, int height) {
    this.width = width;
    this.height = height;
    this.tileMap = new HashMap<>();
    initializeTileMap();
  }

  private void initializeTileMap() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        tileMap.put(new Coordinate(x, y), TileType.ROAD);
      }
    }
  }

  public boolean isInsideMap(Coordinate c) {
    if (c == null) {
      return false;
    }
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
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Coordinate c = new Coordinate(x, y);
        TileType t = getTileType(c);
        char ch;
        if (t == TileType.BUILDING) {
          ch = 'B';
        } else if (t == TileType.PATH) {
          ch = 'P';
        } else {
          ch = 'R';
        }
        sb.append(ch);
      }
      sb.append('\n');
    }
    return sb.toString();
  }

}
