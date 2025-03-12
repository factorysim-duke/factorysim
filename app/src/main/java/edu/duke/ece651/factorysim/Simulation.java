package edu.duke.ece651.factorysim;

/**
 * Controls the buildings, time, etc. of the overall simulation.
 *
 * NOTE: This class is current a placeholder because `CommandHandler` depends on it.
 *       If it's causing any conflicts, feel free to delete it.
 */
public class Simulation {
    /**
     * This causes the simulation to run for `n` time-steps.
     *
     * @param n the number of steps for the simulation to run.
     *          It is at least 1 and is less than `Integer.MAX_VALUE`.
     * @throws IllegalArgumentException when `n` is less than 1 or equal to `Integer.MAX_VALUE`.
     */
    public void step(int n) {
        if (n < 1 || n == Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        // TODO
    }

    /**
     *  This causes the simulation to run until all user-made requests are completed.
     */
    public void finish() {
        // TODO
    }
}
