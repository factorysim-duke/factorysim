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

        Map<String, Type> types = buildTypes(configData.types);
        Map<String, Recipe> recipes = buildRecipes(configData.recipes);
        Map<String, Building> buildings = buildBuildings(configData.buildings, types);

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
                        + "', but that is not defined as a recipe output (#6).");
                }
            }
          }

        return recipes;
    }

    /**
     * Builds the types from the TypeDTOs and checks the validity of the data.
     * 
     * @param typeDTOs is the TypeDTOs to build the types from.
     * @return the Map of types.
     */
    private static Map<String, Type> buildTypes(List<TypeDTO> typeDTOs) {
        Map<String, Type> types = new HashMap<>();
        Set<String> usedNames = new HashSet<>();
        for (TypeDTO typeDTO : typeDTOs) {
            Utils.validNameAndUnique(typeDTO.name, usedNames);
            usedNames.add(typeDTO.name);
        }
        return types;
    }

    private static void typeCheck(List<String> types, Map<String, Type> typeMap) {
        for (String type : types) {
            if (!typeMap.containsKey(type)) {
                throw new IllegalArgumentException("Type is not defined: " + type);
            }
        }
    }

    private static Map<String, Building> buildBuildings(List<BuildingDTO> buildingDTOs,
            Map<String, Type> typeMap) {
        Map<String, Building> buildings = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<String> types = new ArrayList<>();
        for (BuildingDTO buildingDTO : buildingDTOs) {
            names.add(buildingDTO.name);
            types.add(buildingDTO.type);
        }
        typeCheck(types, typeMap);
        return buildings;
    }
}
