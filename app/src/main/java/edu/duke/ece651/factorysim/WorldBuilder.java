package edu.duke.ece651.factorysim;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

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
     * @return the World object.
     */
    public static World buildWorld(ConfigData configData) {
        Utils.nullCheck(configData, "ConfigData is null");
        Utils.nullCheck(configData.buildings, "Buildings are null");
        Utils.nullCheck(configData.types, "Types are null");
        Utils.nullCheck(configData.recipes, "Recipes are null");

        Map<String, Recipe> recipes = buildRecipes(configData.recipes);
        Map<String, Type> types = buildTypes(configData.types, recipes);
        Map<String, Building> buildings = buildBuildings(configData.buildings, types, recipes);
        validateBuildingsIngredients(buildings, types);

        World world = new World();
        world.types = new ArrayList<>(types.values());
        world.recipes = new ArrayList<>(recipes.values());
        world.buildings = new ArrayList<>(buildings.values());

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
            HashMap<Item, Integer> ingredients = new HashMap<>();
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
     * @param recipes is the Map of recipes.
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
     * Builds the buildings from the BuildingDTOs and checks the validity of the data.
     * 
     * @param buildingDTOs is the BuildingDTOs to build the buildings from.
     * @param typeMap is the Map of types.
     * @param recipeMap is the Map of recipes.
     * @return the Map of buildings.
     */
    private static Map<String, Building> buildBuildings(List<BuildingDTO> buildingDTOs,
            Map<String, Type> typeMap, Map<String, Recipe> recipeMap) {
        Map<String, Building> buildings = new HashMap<>();
        Set<String> usedNames = new HashSet<>();
        for (BuildingDTO buildingDTO : buildingDTOs) {
            Utils.validNameAndUnique(buildingDTO.name, usedNames);
            usedNames.add(buildingDTO.name);

            if (buildingDTO.mine != null) {
                Recipe recipe = recipeMap.get(buildingDTO.mine);
                Utils.nullCheck(recipe, "Mine building '" + buildingDTO.name + "' has no recipe (violates #8).");
                if (!buildingDTO.getSources().isEmpty()) {
                    throw new IllegalArgumentException("Mine building '" + buildingDTO.name + "' has sources (violates #4).");
                }
                if (!recipe.getIngredients().isEmpty()) {
                    throw new IllegalArgumentException("Mine building '" + buildingDTO.name + "' should have no ingredients (violates #8).");
                }
                MineBuilding mineBuilding = new MineBuilding(recipe, buildingDTO.name);
                buildings.put(buildingDTO.name, mineBuilding);
            } else if (buildingDTO.type != null) {
                if (!typeMap.containsKey(buildingDTO.type)) {
                    throw new IllegalArgumentException("Type '" + buildingDTO.type + "' is not defined (violates #2).");
                }
                // TODO: Can Factory building have no sources?
                if (buildingDTO.getSources().isEmpty()) {
                    throw new IllegalArgumentException("Factory building '" + buildingDTO.name + "' has no sources.");
                }
                Type type = typeMap.get(buildingDTO.type);
                FactoryBuilding factoryBuilding = new FactoryBuilding(type, buildingDTO.name, new ArrayList<>());
                buildings.put(buildingDTO.name, factoryBuilding);
            } else {
                throw new IllegalArgumentException("Building '" + buildingDTO.name + "' has no mine or type.");
            }
        }

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
        
        return buildings;
    }

    /**
     * Validates the ingredients of the buildings.
     * 
     * @param buildings is the Map of buildings.
     * @param types is the Map of types.
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
                        throw new IllegalArgumentException("Factory building '" + factoryBuilding.getName() + "' has ingredient '" + ingredient.getName() + "' (violates #9).");
                    }
                }
            }
        }
    }
}
