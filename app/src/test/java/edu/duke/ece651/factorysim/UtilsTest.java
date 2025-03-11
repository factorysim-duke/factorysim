package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class UtilsTest {
  @Test
  public void test_invalidString() {
    assertEquals("'", Utils.notAllowedInName);
  }

  @Test
  public void test_nullName() {
    assertThrows(IllegalArgumentException.class, () -> Utils.validNameAndUnique("", new HashSet<>()));
  }

  @Test
  public void test_invalidName() {
    assertThrows(IllegalArgumentException.class, () -> Utils.validNameAndUnique("'a", new HashSet<>()));
  }

  @Test
  public void test_nameNotUnique() {
    HashSet<String> usedNames = new HashSet<>();
    usedNames.add("a");
    assertThrows(IllegalArgumentException.class, () -> Utils.validNameAndUnique("a", usedNames));
  }

  @Test
  public void test_nameValidAndUnique() {
    HashSet<String> usedNames = new HashSet<>();
    usedNames.add("a");
    Utils.validNameAndUnique("b", usedNames);
  }

  @Test
  public void test_nullCheck() {
    assertThrows(IllegalArgumentException.class, () -> Utils.nullCheck(null, "message"));
    assertDoesNotThrow(() -> Utils.nullCheck("a", "message"));
  }

  @Test
  public void test_validLatency() {
    assertThrows(IllegalArgumentException.class, () -> Utils.validLatency(0));
    assertThrows(IllegalArgumentException.class, () -> Utils.validLatency(Integer.MAX_VALUE + 1));
    assertDoesNotThrow(() -> Utils.validLatency(1));
    assertDoesNotThrow(() -> Utils.validLatency(Integer.MAX_VALUE));
  }
}