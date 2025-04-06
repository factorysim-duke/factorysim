package edu.duke.ece651.factorysim;

/**
 * Represents the coordinate of a given place with row and column.
 * This file from Shiyu's battleship homework.
 */
public class Coordinate {
  private final int x;
  private final int y;

  /**
   * Constructs a Coordinate with given row and column.
   * 
   * @param x   is the row number of the coordinate.
   * @param y is the column number of the coordinate.
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the x of the Coordinate.
   * 
   * @return the x of the Coordinate.
   */
  public int getX() {
    return x;
  }

  /**
   * Gets the y of the Coordinate.
   * 
   * @return the y of the Coordinate.
   */
  public int getY() {
    return y;
  }

  /**
   * Compares two Coordinates to see if they are equal: same x and y.
   * 
   * @param object is the Object being compared to.
   * @return true if the two Coordinates have the same x and y, false otherwise.
   */
  @Override
  public boolean equals(Object object) {
    if (object.getClass().equals(getClass())) {
      Coordinate coordinate = (Coordinate) object;
      return x == coordinate.x && y == coordinate.y;
    }
    return false;
  }

  /**
   * Gives textual representation of the Coordinate, e.g. (1, 2)
   * 
   * @return textual representation of the Coordinate.
   */
  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  /**
   * Generates hashcode for a Coordinate.
   * (Use the fact that Java's String have a good hashcode)
   * 
   * @return hashcode of a Coordinate.
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public Coordinate getNextCoord(int dir) {
    switch (dir) {
      case 0:
        return new Coordinate(x, y - 1);
      case 1:
        return new Coordinate(x + 1, y);
      case 2:
        return new Coordinate(x, y + 1);
      case 3:
        return new Coordinate(x - 1, y);
      default:
        throw new IllegalArgumentException("Invalid direction: " + dir);
    }
  }
}
