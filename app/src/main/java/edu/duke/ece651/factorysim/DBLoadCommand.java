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
