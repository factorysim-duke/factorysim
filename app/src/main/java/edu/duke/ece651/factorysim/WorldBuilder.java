package edu.duke.ece651.factorysim;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


/**
 * WorldBuilder is a class for building the game world from the ConfigData.
 * It will create all the buildings, types and recipes, and check the validity of the
 * them.
 */
public class WorldBuilder {

    private static void nameCheck(List<String> names) {
        Set<String> nameSet = new HashSet<>(names);
        if (nameSet.size() != names.size()) {
            throw new IllegalArgumentException("Name must be unique, but is: " + names);
        }
        for (String name : names) {
            if (name.contains("'")) {
                throw new IllegalArgumentException("Name cannot contain " + Utils.notAllowedInName + ", but is: " + name);
            }
        }
    }

    private static Map<String, Type> buildTypes(List<TypeDTO> typeDTOs) {
        Map<String, Type> types = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (TypeDTO typeDTO : typeDTOs) {
            names.add(typeDTO.name);
        }
        nameCheck(names);
        return types;
    }

    private static Map<String, Recipe> buildRecipes(List<RecipeDTO> recipeDTOs) {
        Map<String, Recipe> recipes = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (RecipeDTO recipeDTO : recipeDTOs) {
            names.add(recipeDTO.output);
        }
        nameCheck(names);
        return recipes;
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
        nameCheck(names);
        typeCheck(types, typeMap);
        return buildings;
    }
    

    public static World buildWorld(ConfigData configData) {
        if (configData == null) {
            throw new IllegalArgumentException("ConfigData is null");
        }
        if (configData.buildings == null || configData.types == null || configData.recipes == null) {
            throw new IllegalArgumentException("ConfigData is invalid");
        }

        Map<String, Type> types = buildTypes(configData.types);
        Map<String, Recipe> recipes = buildRecipes(configData.recipes); 
        Map<String, Building> buildings = buildBuildings(configData.buildings, types);

        World world = new World();
        world.types = new ArrayList<>(types.values());
        world.recipes = new ArrayList<>(recipes.values());
        world.buildings = new ArrayList<>(buildings.values());

        return world;
    }
}
