package edu.duke.ece651.factorysim;

import edu.duke.ece651.factorysim.client.ServerConnectionManager;

public class DBSaveCommand implements Command {
    @Override
    public String getName() {
        return "dbsave";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        try {
            ServerConnectionManager.getInstance().saveUserSave(sim.toJson());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
