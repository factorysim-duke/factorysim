package edu.duke.ece651.factorysim;

import java.io.IOException;

/**
 * Usage: <code>save file name</code>
 */
public class SaveCommand implements Command {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public void execute(String[] args, Simulation sim){
        // Check argument count
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid 'save' command: illegal number of arguments");
        }

        // Make sure the first argument is "save"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'save' command: command name doesn't match");
        }

        // Parse steps
        String fileName = args[1];

            sim.save(fileName);

    }
}
