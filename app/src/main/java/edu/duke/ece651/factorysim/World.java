package edu.duke.ece651.factorysim;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a world in the simulation which holds all the information.
 */
public class World {
  private List<Building> buildings;
  private List<Type> types;
  private List<Recipe> recipes;
  private HashMap<Building, Coordinate> coordinateMap;
  
  /**
   * Gets all the buildings of the world.
   * 
   * @return the list of buildings in the world.
   */
  public List<Building> getBuildings() {
    return buildings;
  }

  /**
   * Gets all the factory types in the world.
   * 
   * @return the list of factory types in the world.
   */
  public List<Type> getTypes() {
    return types;
  }

  /**
   * Gets all the recipes in the world.
   * 
   * @return the list of recipes in the world.
   */
  public List<Recipe> getRecipes() {
    return recipes;
  }

  /**
   * Sets the buildings in the world.
   * 
   * @param buildings is the list of buildings to set.
   */
  public void setBuildings(List<Building> buildings) {
    this.buildings = buildings;
  }

  /**
   * Sets the factory types in the world.
   * 
   * @param types is the list of factory types to set.
   */
  public void setTypes(List<Type> types) {
    this.types = types;
  }

  /**
   * Sets the recipes in the world.
   * 
   * @param recipes is the list of recipes to set.
   */
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
   * 
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
   * 
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

  public void removeBuildingFromCoordinateMap(){
  }

  public void updateCoordinateMap(Building building, int x, int y) {
  }

  public void updateCoordinateMap(Building building, Coordinate coordinate){
  }
}

