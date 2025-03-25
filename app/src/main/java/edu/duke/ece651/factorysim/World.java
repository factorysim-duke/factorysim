package edu.duke.ece651.factorysim;

import java.util.List;

public class World {
  private List<Building> buildings;
  private List<Type> types;
  private List<Recipe> recipes;

  public List<Building> getBuildings() {
    return buildings;
  }

  public List<Type> getTypes() {
    return types;
  }

  public List<Recipe> getRecipes() {
    return recipes;
  }

  public void setBuildings(List<Building> buildings) {
    this.buildings = buildings;
  }

  public void setTypes(List<Type> types) {
    this.types = types;
  }

  public void setRecipes(List<Recipe> recipes) {
    this.recipes = recipes;
  }

  /**
   * Gets the building from name.
   * 
   * @return the building with name if the building exists, null otherwise.
   */
  public Building getBuildingFromName(String name) {
    for (Building building : buildings) {
      if (building.getName().equals(name)) {
        return building;
      }
    }
    return null;
  }

  /**
   * Gets the recipe for an item.
   * 
   * @return the recipe if exists, null otherwise.
   */
  public Recipe getRecipeForItem(Item item) {
    for (Recipe recipe : recipes) {
      if (recipe.getOutput().getName().equals(item.getName())) {
        return recipe;
      }
    }
    return null;
  }

  /**
   * Checks if the world has a building with the given name.
   * 
   * @param name the name of the building to check
   * @return true if the building exists, false otherwise
   */
  public boolean hasBuilding(String name) {
    for (Building building : buildings) {
      if (building.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets a Type by its name.
   * @param name The name of the type.
   * @return The Type object, or null if not found.
   */
  public Type getTypeFromName(String name) {
    for (Type type : types) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    return null;
  }

  /**
   * Gets a Recipe by its name (output item name).
   * @param name The name of the recipe's output item.
   * @return The Recipe object, or null if not found.
   */
  public Recipe getRecipeFromName(String name) {
    for (Recipe recipe : recipes) {
      if (recipe.getOutput().getName().equals(name)) {
        return recipe;
      }
    }
    return null;
  }


}
