package edu.duke.ece651.factorysim;

import java.util.LinkedHashMap;

/**
 * Data Transfer Object for waste disposal buildings.
 */
public class WasteDisposalDTO {
  public String name;
  public Integer x;
  public Integer y;
  public LinkedHashMap<String, WasteConfig> wasteTypes = new LinkedHashMap<>();

  /**
   * Configuration for a waste type.
   */
  public static class WasteConfig {
    public int capacity;
    public int disposalRate;
    public int timeSteps;
  }
}
