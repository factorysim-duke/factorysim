package edu.duke.ece651.factorysim;

import java.util.Set;

/**
 * Provides some useful functions for global use.
 */
public class Utils {
  public static String notAllowedInName = "'";

  /**
   * Tells if the name is valid.
   * 
   * @param name is the name to check.
   * @return true if the name is valid (i.e. not containing '), false otherwise.
   */
  public static boolean isNameValid(String name) {
    if (name.contains(notAllowedInName) || name.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Checks if the name is valid and unique.
   * 
   * @param name is the name to check.
   * @param usedNames is the set of used names.
   */
  public static void validNameAndUnique(String name, Set<String> usedNames) {
    if (!isNameValid(name)) {
      throw new IllegalArgumentException("Name is invalid: " + name);
    }
    if (usedNames.contains(name)) {
      throw new IllegalArgumentException("Name must be unique: " + name);
    }
  }

  /**
   * Checks if the object is null.
   * 
   * @param o is the object to check.
   * @param message is the message to throw if the object is null.
   */ 
  public static void nullCheck(Object o, String message) {
    if (o == null) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Checks if the latency is valid (non-negative and less than Integer.MAX_VALUE).
   * 
   * @param latency is the latency to check.
   */
  public static void validLatency(int latency) {
    if (latency < 1) {
      throw new IllegalArgumentException("Latency must be non-negative: " + latency);
    }
  }
}