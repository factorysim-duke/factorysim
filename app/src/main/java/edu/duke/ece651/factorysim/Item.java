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
   * @throws IllegalArgumentException if the name is not valid.
   */
  public Item(String name) {
    if (Utils.isNameValid(name) == false) {
      throw new IllegalArgumentException("Item name cannot contain " + Utils.notAllowedInName + ", but is: " + name);
    }
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
