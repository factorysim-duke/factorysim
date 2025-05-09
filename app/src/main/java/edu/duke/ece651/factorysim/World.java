package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * Represents a world in the simulation which holds all the information.
 */
public class World {
  private List<Building> buildings;
  private List<Type> types;
  private List<Recipe> recipes;
  public HashMap<Building, Coordinate> locationMap;
  public TileMap tileMap;

  public Map<String, WasteDisposalDTO.WasteConfig> wasteConfigMap;

  /**
   * Constructs an empty world.
   */
  public World() {
    this.buildings = null;
    this.types = null;
    this.recipes = null;
    this.locationMap = new HashMap<>();
    this.tileMap = null;
    this.wasteConfigMap = new HashMap<>();
  }

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

  /**
   * Generates location map from the list of buildings.
   */
  public void generateLocationMap() {
    for (Building building : buildings) {
      locationMap.put(building, building.getLocation());
    }
  }

  public void updateTileMap() {
    for (Building building : buildings) {
      tileMap.setTileType(building.getLocation(), TileType.BUILDING);
    }
  }

  public TileMap getTileMap() {
    return tileMap;
  }

  /**
   * Sets the dimensions of the tile map.
   *
   * @param width  is the width of the board.
   * @param height is the height of the board.
   */
  public void setTileMapDimensions(int width, int height) {
    this.tileMap = new TileMap(width, height);
  }

  /**
   * Check if a location is occupied by any building in the world.
   *
   * @param c is the location to check.
   * @return whether the location is occupied by any building in the world.
   */
  public boolean isOccupied(Coordinate c) {
    return locationMap.containsValue(c);
  }

  /**
   * Tries to add an existing building to the world.
   *
   * @param building is the building instance to add.
   * @return true if building is added successfully, false otherwise.
   */
  public boolean tryAddBuilding(Building building) {
    Coordinate location = building.getLocation();
    if (isOccupied(location)) {
      return false;
    }
    buildings.add(building);
    locationMap.put(building, location);
    if (tileMap != null) {
      tileMap.setTileType(location, TileType.BUILDING);
    }
    building.setLocation(location);
    return true;
  }

  /**
   * Checks if a building name is unique, if not, modifies it and returns a name
   * with conflicts resolved.
   *
   * @param name is the name to resolve conflicts.
   * @return resolved unique name.
   */
  public String resolveBuildingNameConflict(String name) {
    String resolved = name;
    int counter = 1;
    while (hasBuilding(resolved)) {
      resolved = name + "_" + counter;
      counter++;
    }
    return resolved;
  }

  /**
   * Removes a building from the world.
   *
   * @param building is the building to remove.
   */
  public void removeBuildingFromWorld(Building building) {
    buildings.remove(building);
    locationMap.remove(building);
    if (building.getLocation() != null) {
      tileMap.setTileType(building.getLocation(), TileType.ROAD);
    }
  }
}
