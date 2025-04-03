package edu.duke.ece651.factorysim;

import com.google.gson.Gson;
import java.io.*;

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
   * @throws com.google.gson.JsonSyntaxException when JSON syntax is bad.
   * @throws com.google.gson.JsonIOException when JSON IO error.
   */
  public static ConfigData loadConfigData(String jsonFilePath) {
    if (jsonFilePath == null) {
      throw new NullPointerException("JSON file path cannot be null.");
    }

    try (FileReader reader = new FileReader(jsonFilePath)) {
      return loadConfigDataFromReader(reader);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Loads the ConfigData from a reader.
   *
   * @param reader is the reader to read the JSON from.
   * @return the ConfigData object.
   * @throws com.google.gson.JsonSyntaxException when JSON syntax is bad.
   * @throws com.google.gson.JsonIOException when JSON IO error.
   */
  static ConfigData loadConfigDataFromReader(Reader reader) {
    return new Gson().fromJson(reader, ConfigData.class);
  }
}
