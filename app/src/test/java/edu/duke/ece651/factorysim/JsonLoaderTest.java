package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

public class JsonLoaderTest {
  @Test
  public void test_JasonLoader_success() {
    String jsonFilePath = "src/test/resources/inputs/Valid.json";
    ConfigData configData = JsonLoader.loadConfigData(jsonFilePath);
    assertNotNull(configData);
    assertEquals(configData.recipes.size(), 2);
    assertEquals(configData.types.size(), 3);
    assertEquals(configData.buildings.size(), 5);
  }

  @Test
  public void test_JasonLoader_failure_null() {
    ConfigData configData = JsonLoader.loadConfigData("null.json");
    assertNull(configData);
  }

  @Test
  public void test_JasonLoader_failure_invalid_json_format() {
    String jsonFilePath = "src/test/resources/inputs/InvalidJSONFormat.txt";
    assertThrows(JsonSyntaxException.class, () -> {
      JsonLoader.loadConfigData(jsonFilePath);
    });
  }
}
