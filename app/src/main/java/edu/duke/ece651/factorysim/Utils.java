package edu.duke.ece651.factorysim;

import java.util.Set;
import java.util.Arrays;

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

  /**
   * Checks if a string is in a string list.
   * 
   * @param s is the string to check.
   * @param list is the list to check.
   * @return true if the string is in the list, false otherwise.
   */
  public static boolean isInList(String s, String[] list) {
    return Arrays.asList(list).contains(s);
  }

  /**
   * Removes the quotes from the string.
   * 
   * @param s is the string to remove the quotes from.
   * @return the string without the quotes.
   */
  public static String removeQuotes(String s) {
    return s.substring(1, s.length() - 1);
  }
}