package edu.duke.ece651.factorysim;

import edu.duke.ece651.factorysim.client.ServerConnectionManager;

public class DBSaveCommand implements Command {
    @Override
    public String getName() {
        return "dbsave";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid 'dbsave' command: illegal number of arguments");
        }

        // Make sure the first argument is "dbsave"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'dbsave' command: command name doesn't match");
        }

        try {
            ServerConnectionManager.getInstance().saveUserSave(sim.toJson());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
