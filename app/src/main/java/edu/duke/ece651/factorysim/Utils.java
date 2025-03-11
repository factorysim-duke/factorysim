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

  public static void validNameAndUnique(String name, Set<String> usedNames) {
    if (!isNameValid(name)) {
      throw new IllegalArgumentException("Name is invalid: " + name);
    }
    if (usedNames.contains(name)) {
      throw new IllegalArgumentException("Name must be unique: " + name);
    }
  }
}