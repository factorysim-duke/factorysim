package edu.duke.ece651.factorysim;

/**
 * Provides some useful functions for global use.
 */
public class Utils {
  public static String notAllowedInName = "'";
  /**
   * Tells if the name is valid.
   * 
   * @return true if the name is valid (i.e. not containing '), false otherwise.
   */
  public static boolean isNameValid(String name) {
    if (name.contains(notAllowedInName)) {
      return false;
    } else {
      return true;
    }
  }
}
