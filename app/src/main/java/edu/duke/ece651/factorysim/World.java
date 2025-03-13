package edu.duke.ece651.factorysim;

import java.util.List;

public class World {
  public List<Building> buildings;
  public List<Type> types;
  public List<Recipe> recipes;

  public List<Building> getBuildings() {
    return buildings;
  }

  public List<Type> getTypes() {
    return types;
  }

  public List<Recipe> getRecipes() {
    return recipes;
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
}
