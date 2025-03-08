package edu.duke.ece651.factorysim;

/**
 * Represents an item (i.e. metal, hinge, door..) in the simulation.
 */
public class Item {
  private final String name;

  /**
   * Constructs an item with given name.
   * 
   * @param name is the item's name.
   */
  public Item(String name) {
    this.name = name;
  }

  /**
   * Gets the name of the item.
   * 
   * @return the name of the item.
   */
  public String getName() {
    return name;
  }
}
