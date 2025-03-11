package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class UtilsTest {
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
}
