package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;

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

        // TODO: Test if step(s) has been made in the simulation
    }

    private static void execute(StepCommand step, Simulation sim, String... commands) {
        step.execute(commands, sim);
    }
}
