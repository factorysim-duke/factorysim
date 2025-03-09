package edu.duke.ece651.factorysim;

import java.util.List;

/**
 * Represents a kind of factory's type in the simulation.
 */
public class Type {
  private final String name;
  private final List<Recipe> recipes;

  /**
   * Constructs a factory type.
   * 
   * @param name    is the factory type's name.
   * @param recipes is the list of recipes of this factory type.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public Type(String name, List<Recipe> recipes) {
    if (Utils.isNameValid(name) == false) {
      throw new IllegalArgumentException("Type name cannot contain " + Utils.notAllowedInName + ", but is: " + name);
    }
    this.name = name;
    this.recipes = recipes;
  }

  /**
   * Gets the name of the factory type.
   * 
   * @return the name of the factory type.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets all the ordered recipes of the factory type.
   * 
   * @return a list of the recipes of this factory type.
   */
  public List<Recipe> getRecipes() {
    return recipes;
  }
}
