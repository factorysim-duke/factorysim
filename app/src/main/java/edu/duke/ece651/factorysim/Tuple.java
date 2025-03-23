package edu.duke.ece651.factorysim;

import java.util.Objects;

/**
 * Represents a simple immutable tuple with two elements.
 */
public class Tuple<T, U> {
  private final T first;
  private final U second;

  public Tuple(T first, U second) {
    this.first = first;
    this.second = second;
  }

  public T first() {
    return this.first;
  }

  public U second() {
    return this.second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !this.getClass().equals(o.getClass())) {
      return false;
    }
    Tuple<?, ?> t = (Tuple<?, ?>)o;
    return Objects.equals(first, t.first) && Objects.equals(second, t.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }
}

