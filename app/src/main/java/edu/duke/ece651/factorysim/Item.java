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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Item other = (Item) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
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
