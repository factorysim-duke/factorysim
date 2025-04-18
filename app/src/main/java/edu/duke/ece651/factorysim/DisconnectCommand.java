package edu.duke.ece651.factorysim;

public class DisconnectCommand implements Command{
    @Override
    public String getName() {
        return "disconnect";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 4) {
            throw new IllegalArgumentException("Invalid 'disconnect' command: illegal number of arguments");
        }

        // Make sure the 1st argument is "connect"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'disconnect' command: command name doesn't match");
        }

        // Make sure the 3rd argument is "from"
        if (!args[2].equals("to")) {
            throw new IllegalArgumentException("Invalid 'disconnect' command: invalid syntax");
        }

        // Make sure building name is quoted
        Utils.throwIfNotQuoted(args[1], "Invalid 'disconnect' command: 2nd argument must be a quoted name");

        // Make sure from building name is quoted
        Utils.throwIfNotQuoted(args[3], "Invalid 'disconnect' command: 4th argument must be a quoted name");

        // Operate on the simulation
        sim.disconnectBuildings(Utils.removeQuotes(args[1]), Utils.removeQuotes(args[3]));
    }
}
