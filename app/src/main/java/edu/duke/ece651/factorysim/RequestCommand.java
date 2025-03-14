package edu.duke.ece651.factorysim;

/**
 * Usage: <code>request 'ITEM' from 'BUILDING'</code>
 */
public class RequestCommand implements Command {
    @Override
    public String getName() {
        return "request";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 4) {
            throw new IllegalArgumentException("Invalid 'request' command: illegal number of arguments");
        }

        // Make sure the 1st argument is "request"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'request' command: command name doesn't match");
        }

        // Make sure the 3rd argument is "from"
        if (!args[2].equals("from")) {
            throw new IllegalArgumentException("Invalid 'request' command: invalid syntax");
        }

        // Make sure item name is quoted
        Utils.throwIfNotQuoted(args[1], "Invalid 'request' command: 2nd argument must be a quoted name");

        // Make sure from building name is quoted
        Utils.throwIfNotQuoted(args[3], "Invalid 'request' command: 4th argument must be a quoted name");

        // Operate on the simulation
        sim.makeUserRequest(Utils.removeQuotes(args[1]), Utils.removeQuotes(args[3]));
    }
}
