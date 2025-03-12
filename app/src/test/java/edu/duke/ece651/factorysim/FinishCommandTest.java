package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FinishCommandTest {
    @Test
    public void test_execute() {
        FinishCommand finish = new FinishCommand();
        Simulation sim = new Simulation();

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { execute(finish, sim, "step", "1"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(finish, sim, "request", "'door'", "from", "'door factory'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(finish, sim, "FINISH"); });

        // TODO: Test if simulation is finished
    }

    private static void execute(FinishCommand finish, Simulation sim, String... commands) {
        finish.execute(commands, sim);
    }
}
