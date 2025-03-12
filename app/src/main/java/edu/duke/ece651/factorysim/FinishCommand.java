package edu.duke.ece651.factorysim;

/**
 * Usage: <code>finish</code>
 */
public class FinishCommand implements Command {
    @Override
    public String getName() {
        return "finish";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid 'finish' command: illegal number of arguments");
        }

        // Make sure the first argument is "finish"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'finish' command: command name doesn't match");
        }

        // Operate on the simulation
        sim.finish();
    }
}
