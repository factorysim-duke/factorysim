package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {
    @Test
    public void test_step() {
        Simulation sim = new Simulation();

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { sim.step(-1); });
        assertThrows(IllegalArgumentException.class, () -> { sim.step(0); });
        assertThrows(IllegalArgumentException.class, () -> { sim.step(Integer.MAX_VALUE); });
    }
}
