package edu.duke.ece651.factorysim;

/**
 * Usage: <code>step N</code>
 */
public class StepCommand implements Command {
    @Override
    public String getName() {
        return "step";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid 'step' command: illegal number of arguments");
        }

        // Make sure the first argument is "step"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'step' command: command name doesn't match");
        }

        // Parse steps
        int n;
        try {
            n = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid 'step' command: bad `N` integer format", e);
        }

        // Operate on the simulation
        try {
            sim.step(n);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid 'step' command: bad `N` range", e);
        }
    }
}
