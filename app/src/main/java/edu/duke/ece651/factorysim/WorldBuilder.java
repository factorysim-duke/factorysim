package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * WorldBuilder is a class for building the game world from the ConfigData.
 * It will create all the buildings, types and recipes, and check the validity
 * of the data.
 */
public class WorldBuilder {

  /**
   * Builds the world from the ConfigData.
   *
   * @param configData is the ConfigData to build the world from.
   * @param simulation is the simulation where this world is used.
   * @return the World object.
   */
  public static World buildWorld(ConfigData configData, Simulation simulation) {
    Utils.nullCheck(configData, "ConfigData is null");
    Utils.nullCheck(configData.buildings, "Buildings are null");
    Utils.nullCheck(configData.types, "Types are null");
    Utils.nullCheck(configData.recipes, "Recipes are null");

    Map<String, Recipe> recipes = buildRecipes(configData.recipes);
    Map<String, Type> types = buildTypes(configData.types, recipes);
    Map<String, Building> buildings = buildBuildings(configData.buildings, types, recipes, simulation);
    validateBuildingsIngredients(buildings, types);
    World world = new World();
    world.setTypes(new ArrayList<>(types.values()));
    world.setRecipes(new ArrayList<>(recipes.values()));
    world.setBuildings(new ArrayList<>(buildings.values()));

    return world;
  }

  /**
   * Builds the recipes from the RecipeDTOs and checks the validity of the data.
   *
   * @param recipeDTOs is the RecipeDTOs to build the recipes from.
   * @return the Map of recipes.
   */
  private static Map<String, Recipe> buildRecipes(List<RecipeDTO> recipeDTOs) {
    Map<String, Recipe> recipes = new HashMap<>();
    Set<String> usedNames = new HashSet<>();

    for (RecipeDTO recipeDTO : recipeDTOs) {
      // Check if the output name is valid and unique
      Utils.validNameAndUnique(recipeDTO.output, usedNames);
      usedNames.add(recipeDTO.output);

      // Check if the latency is valid
      Utils.validLatency(recipeDTO.latency);

      // Create the output item
      Item output = new Item(recipeDTO.output);

      // Create the ingredients map
      LinkedHashMap<Item, Integer> ingredients = new LinkedHashMap<>();
      for (Map.Entry<String, Integer> entry : recipeDTO.ingredients.entrySet()) {
        Item ingredient = new Item(entry.getKey());
        ingredients.put(ingredient, entry.getValue());
      }

      // Create the recipe
      Recipe recipe = new Recipe(output, ingredients, recipeDTO.latency);
      recipes.put(recipeDTO.output, recipe);
    }

    // Check if all ingredients are defined as recipe outputs
    for (Recipe r : recipes.values()) {
      for (Item ingredient : r.getIngredients().keySet()) {
        if (!recipes.containsKey(ingredient.getName())) {
          throw new IllegalArgumentException("Recipe '" + r.getOutput().getName()
              + "' references ingredient '" + ingredient.getName()
              + "', but that is not defined as a recipe output (violates #6).");
        }
      }
    }

    return recipes;
  }

  /**
   * Builds the types from the TypeDTOs and checks the validity of the data.
   *
   * @param typeDTOs is the TypeDTOs to build the types from.
   * @param recipes  is the Map of recipes.
   * @return the Map of types.
   */
  private static Map<String, Type> buildTypes(List<TypeDTO> typeDTOs, Map<String, Recipe> recipes) {
    Map<String, Type> types = new HashMap<>();
    Set<String> usedNames = new HashSet<>();
    for (TypeDTO typeDTO : typeDTOs) {
      Utils.validNameAndUnique(typeDTO.name, usedNames);
      usedNames.add(typeDTO.name);
      List<Recipe> recipesList = new ArrayList<>();
      for (String recipeName : typeDTO.recipes) {
        Recipe recipe = recipes.get(recipeName);
        Utils.nullCheck(recipe, "Recipe in type '" + typeDTO.name + "' is not defined (violates #5).");
        if (recipe.getIngredients().isEmpty()) {
          throw new IllegalArgumentException("Recipe in type '" + typeDTO.name + "' has no ingredients (violates #7).");
        }
        recipesList.add(recipe);
      }
      Type type = new Type(typeDTO.name, recipesList);
      types.put(typeDTO.name, type);
    }
    return types;
  }

  /**
   * Builds the buildings from the BuildingDTOs and checks the validity of the
   * data.
   *
   * @param buildingDTOs is the BuildingDTOs to build the buildings from.
   * @param typeMap      is the Map of types.
   * @param recipeMap    is the Map of recipes.
   * @return the Map of buildings.
   */
  private static Map<String, Building> buildBuildings(List<BuildingDTO> buildingDTOs,
      Map<String, Type> typeMap, Map<String, Recipe> recipeMap, Simulation simulation) {
    Map<String, Building> buildings = new HashMap<>();
    Set<String> usedNames = new HashSet<>();

    // added for evolution 2: make buildings adapt to locations
    Set<Coordinate> usedCoordinates = new HashSet<>();
    List<Building> buildingsWithoutLocations = new ArrayList<>();

    // create buildings and assign coordinates if possible
    for (BuildingDTO buildingDTO : buildingDTOs) {
      Utils.validNameAndUnique(buildingDTO.name, usedNames);
      usedNames.add(buildingDTO.name);
      Building building;

      if (buildingDTO.mine != null) {
        Recipe recipe = recipeMap.get(buildingDTO.mine);
        Utils.nullCheck(recipe, "Mine building '" + buildingDTO.name + "' has no recipe (violates #8).");
        if (!buildingDTO.getSources().isEmpty()) {
          throw new IllegalArgumentException("Mine building '" + buildingDTO.name + "' has sources (violates #4).");
        }
        if (!recipe.getIngredients().isEmpty()) {
          throw new IllegalArgumentException(
              "Mine building '" + buildingDTO.name + "' should have no ingredients (violates #8).");
        }
        MineBuilding mineBuilding = new MineBuilding(recipe, buildingDTO.name, simulation);
        building = mineBuilding;
      } else if (buildingDTO.type != null) {
        if (!typeMap.containsKey(buildingDTO.type)) {
          throw new IllegalArgumentException("Type '" + buildingDTO.type + "' is not defined (violates #2).");
        }
        // TODO: Can Factory building have no sources?
        if (buildingDTO.getSources().isEmpty()) {
          throw new IllegalArgumentException("Factory building '" + buildingDTO.name + "' has no sources.");
        }
        Type type = typeMap.get(buildingDTO.type);
        FactoryBuilding factoryBuilding = new FactoryBuilding(type, buildingDTO.name, new ArrayList<>(), simulation);
        building = factoryBuilding;
      } else {
        throw new IllegalArgumentException("Building '" + buildingDTO.name + "' has no mine or type.");
      }

      // assign storage
      if (buildingDTO.storage != null) {
        for (Map.Entry<String, Integer> entry : buildingDTO.storage.entrySet()) {
          building.addToStorage(new Item(entry.getKey()), entry.getValue());
        }
      }

      // assign locations if provided
      if (buildingDTO.x != null && buildingDTO.y != null) {
        Coordinate location = new Coordinate(buildingDTO.x, buildingDTO.y);
        if (usedCoordinates.contains(location)) {
          throw new IllegalArgumentException(
              "The location " + location + " has already been occupied by another building.");
        }
        building.setLocation(location);
        usedCoordinates.add(location);
      } else {
        buildingsWithoutLocations.add(building);
      }

      buildings.put(buildingDTO.name, building);
    }

    // if no building has initially provided locations, place the first at (0, 0)
    if (usedCoordinates.isEmpty() && !buildingsWithoutLocations.isEmpty()) {
      Building first = buildingsWithoutLocations.remove(0);
      Coordinate location = new Coordinate(0, 0);
      first.setLocation(location);
      usedCoordinates.add(location);
    }

    // assign sources
    for (BuildingDTO buildingDTO : buildingDTOs) {
      Building building = buildings.get(buildingDTO.name);
      List<Building> sources = new ArrayList<>();
      if (!buildingDTO.getSources().isEmpty()) {
        for (String source : buildingDTO.getSources()) {
          if (buildings.containsKey(source)) {
            sources.add(buildings.get(source));
          } else {
            throw new IllegalArgumentException("Source '" + source + "' is not defined (violates #3).");
          }
        }
      } else {
        continue;
      }
      building.updateSources(sources);
    }

    // assign a valid location to buildings that don't have provided locations
    for (Building missingBuilding : buildingsWithoutLocations) {
      Coordinate validLocation = findValidLocation(usedCoordinates);
      missingBuilding.setLocation(validLocation);
      usedCoordinates.add(validLocation);
    }

    return buildings;
  }

  /**
   * Validates the ingredients of the buildings.
   *
   * @param buildings is the Map of buildings.
   * @param types     is the Map of types.
   */
  private static void validateBuildingsIngredients(Map<String, Building> buildings, Map<String, Type> types) {
    for (Building building : buildings.values()) {
      if (building instanceof FactoryBuilding) {
        FactoryBuilding factoryBuilding = (FactoryBuilding) building;
        Type type = types.get(factoryBuilding.getFactoryType().getName());
        Set<Item> ingredients = new HashSet<>();
        for (Recipe recipe : type.getRecipes()) {
          ingredients.addAll(recipe.getIngredients().keySet());
        }
        for (Item ingredient : ingredients) {
          boolean found = false;
          for (Building source : factoryBuilding.getSources()) {
            if (source instanceof MineBuilding && ((MineBuilding) source).canProduce(ingredient)) {
              found = true;
              break;
            }
            if (source instanceof FactoryBuilding && ((FactoryBuilding) source).canProduce(ingredient)) {
              found = true;
              break;
            }
          }
          if (!found) {
            throw new IllegalArgumentException("Factory building '" + factoryBuilding.getName() + "' has ingredient '"
                + ingredient.getName() + "' (violates #9).");
          }
        }
      }
    }
  }

  /**
   * Finds a valid location for a building accoding to these rules:
   * 1. The location is at least 5 units away in both x and y from any other
   * buildings.
   * 2. The location is within 10 units in x and y of at least one used
   * coordinate.
   * Precondition: the building is not initially assigned a location in JSON.
   * 
   * @param usedCoordinates is the set of coordinates that are already assigned
   *                        with a building.
   * @return a valid Coordinate for a new building placement if there's one, null
   *         otherwise.
   */
  private static Coordinate findValidLocation(Set<Coordinate> usedCoordinates) {
    // TODO: Adjust the search boundaries later if we want to use boards
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (Coordinate c : usedCoordinates) {
      minX = Math.min(minX, c.getX());
      maxX = Math.max(maxX, c.getX());
      minY = Math.min(minY, c.getY());
      maxY = Math.max(maxY, c.getY());
    }
    for (int x = minX - 10; x <= maxX + 10; x++) {
      for (int y = minY - 10; y <= maxY + 10; y++) {
        Coordinate location = new Coordinate(x, y);
        if (!isNotTooCloseToOthers(location, usedCoordinates))
          continue;
        if (!isNotTooFarFromOthers(location, usedCoordinates))
          continue;
        return location;
      }
    }
    return null;
  }

  /**
   * See if a given location is not too close to any other buildings (i.e. at
   * least 5 units away in both x and y dimensions).
   * 
   * @param location        is the location to be checked.
   * @param usedCoordinates is the set of coordinates occupied by other buildings.
   * @return true if the new location is not too close to other existing
   *         locations, false otherwise.
   */
  private static boolean isNotTooCloseToOthers(Coordinate location, Set<Coordinate> usedCoordinates) {
    for (Coordinate c : usedCoordinates) {
      if (Math.abs(location.getX() - c.getX()) < 5 || Math.abs(location.getY() - c.getY()) < 5) {
        return false;
      }
    }
    return true;
  }

  /**
   * See if a given location is not too close to any other buildings (i.e. within
   * 10 units in both x and y dimensions from at least one location).
   * 
   * @param location        is the location to be checked.
   * @param usedCoordinates is the set of coordinates occupied by other buildings.
   * @return true if the new location is not too far away from other existing
   *         locations, false otherwise.
   */
  private static boolean isNotTooFarFromOthers(Coordinate location, Set<Coordinate> usedCoordinates) {
    boolean withinX = false;
    boolean withinY = false;
    for (Coordinate c : usedCoordinates) {
      if (Math.abs(location.getX() - c.getX()) <= 10) {
        withinX = true;
      }
      if (Math.abs(location.getY() - c.getY()) <= 10) {
        withinY = true;
      }
    }
    return withinX && withinY;
  }
}
