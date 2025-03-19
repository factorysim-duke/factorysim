package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VerboseCommandTest {
  @Test
  public void test_execute() {
    VerboseCommand verbose = new VerboseCommand();
    Simulation sim = new TestUtils.MockSimulation();

    // Invalid inputs
    assertThrows(IllegalArgumentException.class, () -> { execute(verbose, sim, "verbose", "1", "2"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "request", "'handle'", "from", "'Ha'"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "finish"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "verbose", "0xFF"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "varbose", "1"); });
    // Note: the following tests is considered invalid because `Simulation` doesn't allow negative verbosity at the
    //       time this test was written
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "verbose", "-1"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "verbose", "-2"); });
    assertThrows(IllegalArgumentException.class,
            () -> { execute(verbose, sim, "verbose", "-3"); });

    // Valid inputs
    execute(verbose, sim, "verbose", "0");
    assertEquals(0, sim.getVerbosity());

    execute(verbose, sim, "verbose", "1");
    assertEquals(1, sim.getVerbosity());

    execute(verbose, sim, "verbose", "2");
    assertEquals(2, sim.getVerbosity());

    // Note: at the time the test was written, verbosity > 2 don't do anything and it's allowed
    execute(verbose, sim, "verbose", "3");
    assertEquals(3, sim.getVerbosity());

    execute(verbose, sim, "verbose", "100");
    assertEquals(100, sim.getVerbosity());
  }

  private static void execute(VerboseCommand verbose, Simulation sim, String... commands) {
    verbose.execute(commands, sim);
  }
}
