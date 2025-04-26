package edu.duke.ece651.factorysim;

import edu.duke.ece651.factorysim.client.ServerConnectionManager;
import java.io.File;

public class DBLoadCommand implements Command {
    @Override
    public String getName() {
        return "dbload";
    }

    @Override
    public void execute(String[] args, Simulation sim) {
        // Check argument count
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid 'dbload' command: illegal number of arguments");
        }

        // Make sure the first argument is "dbload"
        if (!args[0].equals(getName())) {
            throw new IllegalArgumentException("Invalid 'dbload' command: command name doesn't match");
        }

        try {
            String json = ServerConnectionManager.getInstance().loadUserSave();
            File tempFile = App.createTempFile("temp", ".json", json);
            sim.load(tempFile.getAbsolutePath());
            App.deleteTempFile(tempFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
