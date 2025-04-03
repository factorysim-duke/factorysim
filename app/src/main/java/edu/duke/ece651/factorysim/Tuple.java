package edu.duke.ece651.factorysim;

import java.util.Objects;

/**
 * Represents a simple immutable tuple with two elements.
 * 
 * @param <T> is the first element type.
 * @param <U> is the second param type.
 */
public class Tuple<T, U> {
  private final T first;
  private final U second;

  /**
   * Constucts a tuple.
   * 
   * @param first  is the first element.
   * @param second is the second element.
   */
  public Tuple(T first, U second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Gets the first element.
   * 
   * @return the fitst element of tuple.
   */
  public T first() {
    return this.first;
  }

  /**
   * Gets the second element.
   * 
   * @return the second element of tuple.
   */
  public U second() {
    return this.second;
  }

  /**
   * Decides if two tuples are equal or not.
   * 
   * @param o is the object to be compared to.
   * @return true if the two tuples are considered the same, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !this.getClass().equals(o.getClass())) {
      return false;
    }
    Tuple<?, ?> t = (Tuple<?, ?>) o;
    return Objects.equals(first, t.first) && Objects.equals(second, t.second);
  }

  /**
   * Calculates the hashcode of tuple.
   * 
   * @return the hashcode of tuple.
   */
  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  /**
   * Gets the String representation of tuple.
   * 
   * @return the string of the tuple.
   */
  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }
}
