package edu.duke.ece651.factorysim;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

/*
 * JsonLoader is a class for loading JSON data from a file.
 * It is used to transfer JSON data to ConfigData.
 */
public class JsonLoader {

  /**
   * Loads the ConfigData from the given JSON file path.
   * 
   * @param jsonFilePath is the path to the JSON file.
   * @return the ConfigData object.
   * @throws NullPointerException if the file is not found or cannot be read.
   */
  public static ConfigData loadConfigData(String jsonFilePath) {
    if (jsonFilePath == null) {
      throw new NullPointerException("JSON file path cannot be null.");
    }

    Gson gson = new Gson();
    try (FileReader reader = new FileReader(jsonFilePath)) {
      return gson.fromJson(reader, ConfigData.class);
    } catch (IOException e) {
      return null;
    }
  }
}
