package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class ConfigDataTest {
  @Test
  public void test_ConfigData_success() {
    String jsonFilePath = "src/test/resources/inputs/Valid.json";
    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    assertNotNull(configData);
  }

  @Test
  public void test_ConfigData_success_more_fields() {
    String jsonFilePath = "src/test/resources/inputs/ValidMoreFields.json";
    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    assertNotNull(configData);
    assertEquals(configData.recipes.size(), 2);
    assertEquals(configData.types.size(), 3);
    assertEquals(configData.buildings.size(), 5);
  }
}
