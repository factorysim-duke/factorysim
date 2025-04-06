package edu.duke.ece651.factorysim;

/**
 * Usage: <code>connect 'BUILDING' TO 'BUILDING'</code>
 */
public class ConnectCommand implements Command {
    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 4) {
            throw new IllegalArgumentException("Invalid 'connect' command: illegal number of arguments");
        }

        // Make sure the 1st argument is "connect"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'connect' command: command name doesn't match");
        }

        // Make sure the 3rd argument is "from"
        if (!args[2].equals("to")) {
            throw new IllegalArgumentException("Invalid 'connect' command: invalid syntax");
        }

        // Make sure building name is quoted
        Utils.throwIfNotQuoted(args[1], "Invalid 'connect' command: 2nd argument must be a quoted name");

        // Make sure from building name is quoted
        Utils.throwIfNotQuoted(args[3], "Invalid 'connect' command: 4th argument must be a quoted name");

        // Operate on the simulation
        sim.connectBuildings(Utils.removeQuotes(args[1]), Utils.removeQuotes(args[3]));
    }
}
