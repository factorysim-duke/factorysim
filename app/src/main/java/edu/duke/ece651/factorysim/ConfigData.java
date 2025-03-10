package edu.duke.ece651.factorysim;

import java.util.List;

/*
 * ConfigData is a data transfer object for reading JSON data.
 * It is used to transfer ConfigData JSON file to objects.
 */
public class ConfigData {
    public List<RecipeDTO> recipes;
    public List<TypeDTO> types;
    public List<BuildingDTO> buildings;
}