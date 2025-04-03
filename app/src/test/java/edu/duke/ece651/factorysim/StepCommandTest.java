package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StepCommandTest {
    @Test
    public void test_execute() {
        StepCommand step = new StepCommand();
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { execute(step, sim, "step", "1", "2"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(step, sim, "request", "'handle'", "from", "'Ha'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(step, sim, "finish"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(step, sim, "step", "0xFF"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(step, sim, "step", "0"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(step, sim, "stop", "1"); });

        // Test if step(s) has been made in the simulation
        execute(step, sim, "step", "1");
        assertEquals(1, sim.getCurrentTime());

        execute(step, sim, "step", "5");
        assertEquals(6, sim.getCurrentTime());
    }

    private static void execute(StepCommand step, Simulation sim, String... commands) {
        step.execute(commands, sim);
    }
}
