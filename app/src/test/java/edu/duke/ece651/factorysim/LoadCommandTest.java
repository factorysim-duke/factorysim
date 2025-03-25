package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadCommandTest {
    @Test
    public void test_execute() {
        LoadCommand load = new LoadCommand();
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { execute(load, sim, "load", "1.json", "2"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(load, sim, "request", "'handle'", "from", "'Ha'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(load, sim, "finish"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(load, sim, "step", "0xFF"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(load, sim, "step", "1"); });

        assertThrows(IllegalArgumentException.class,
                () -> { execute(load, sim, "load", "00001"); });
        // Test if load has been made in the simulation
        execute(load, sim, "load", "1");

    }

    private static void execute(LoadCommand load, Simulation sim, String... commands) {
        load.execute(commands, sim);
    }
}
