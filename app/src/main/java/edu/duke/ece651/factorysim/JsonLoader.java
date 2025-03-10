package edu.duke.ece651.factorysim;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

/*
 * JsonLoader is a class for loading JSON data from a file.
 * It is used to transfer JSON data to ConfigData.
 */
public class JsonLoader {
  public static ConfigData loadConfigData(String jsonFilePath) {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(jsonFilePath)) {
      return gson.fromJson(reader, ConfigData.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
