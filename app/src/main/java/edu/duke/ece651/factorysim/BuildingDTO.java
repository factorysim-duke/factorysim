package edu.duke.ece651.factorysim;

import java.util.ArrayList;
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
  public Integer x;
  public Integer y;
  public String stores;
  public Integer capacity;
  public Double priority;
  public List<DroneDTO> drones;

  /**
   * Gets the source building names for a building.
   * 
   * @return the list of source building names of the building, or an empty list
   *         if sources is null.
   */
  public List<String> getSources() {
    if (sources == null) {
      return new ArrayList<>();
    } else {
      return sources;
    }
  }
}
