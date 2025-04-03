package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SaveCommandTest {
    @Test
    public void test_execute() throws IOException {
        SaveCommand save = new SaveCommand();
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { execute(save, sim, "save", "1", "2"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(save, sim, "request", "'handle'", "from", "'Ha'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(save, sim, "finish"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(save, sim, "step", "0xFF"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(save, sim, "step", "1"); });

        // Test if step(s) has been made in the simulation
        execute(save, sim, "save", "1");

    }

    private static void execute(SaveCommand save, Simulation sim, String... commands) throws IOException {
        save.execute(commands, sim);
    }
}
