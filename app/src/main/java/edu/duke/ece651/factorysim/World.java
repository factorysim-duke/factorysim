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
}
