package edu.duke.ece651.factorysim;

import java.util.List;
import java.util.Map;

/*
 * BuildingDTO is a data transfer object for reading Building JSON data.
 * It is used to transfer Building JSON data to building objects.
 */
public class BuildingDTO {
    public String name;
    public String type;
    public String mine;
    public List<String> sources;
    public Map<String, Integer> storage;

    public List<String> getSources() {
        return sources;
    }

}