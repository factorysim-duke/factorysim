package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestCommandTest {
    @Test
    public void test_execute() {
        RequestCommand request = new RequestCommand();
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");

        // Invalid inputs
        assertThrows(IllegalArgumentException.class, () -> { execute(request, sim, "step", "3"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(request, sim, "ask", "'door'", "from", "'door factory'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(request, sim, "request", "'door'", "to", "'door factory'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(request, sim, "request", "door", "from", "'door factory'"); });
        assertThrows(IllegalArgumentException.class,
                () -> { execute(request, sim, "request", "'handle'", "from", "Ha"); });

        // TODO: Test if request has been made in `Simulation` (maybe with a mock simulation?)
    }

    private static void execute(RequestCommand request, Simulation sim, String... commands) {
        request.execute(commands, sim);
    }
}