package edu.duke.ece651.factorysim;

/**
 * Represents a simple immutable tuple with two elements.
 */
public record Tuple<T, U>(T first, U second) { }
