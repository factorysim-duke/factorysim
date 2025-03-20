package edu.duke.ece651.factorysim;

import java.util.LinkedHashMap;

/*
 * RecipeDTO is a data transfer object for reading Recipe JSON data.
 * It is used to transfer Recipe JSON data to recipe objects.
 */
public class RecipeDTO {
    public String output;
    public LinkedHashMap<String, Integer> ingredients;
    public int latency;
}
