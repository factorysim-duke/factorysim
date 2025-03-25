package edu.duke.ece651.factorysim;

import java.io.FileNotFoundException;

/**
 * Usage: <code>load file name</code>
 */
public class LoadCommand implements Command {
    @Override
    public String getName() {
        return "load";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid 'load' command: illegal number of arguments");
        }

        // Make sure the first argument is "load"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'load' command: command name doesn't match");
        }

        // Parse steps
        String fileName = args[1];

        // Operate on the simulation

            sim.load(fileName);

    }
}
