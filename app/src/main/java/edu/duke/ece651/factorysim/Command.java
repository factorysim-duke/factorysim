package edu.duke.ece651.factorysim;

/**
 * General interface for a command used by `CommandHandler` to operate on a `Simulation` instance.
 */
public interface Command {
    /**
     * Returns the name of the command which must match the first argument of the command.
     *
     * @return the name of the command.
     */
    String getName();

    /**
     * Execute the command based on an array of argument inputs.
     *
     * @param args command argument strings.
     * @param sim the simulation instance to execute the command on.
     * @throws IllegalArgumentException when command arguments are illegal.
     */
    void execute(String[] args, Simulation sim);
}
