package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestCommandTest {
    @Test
    public void test_execute() {
        RequestCommand request = new RequestCommand();
        Simulation sim = new Simulation();

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

    @Test
    public void test_throwIfNotQuoted() {
        // Valid inputs
        assertDoesNotThrow(() -> { RequestCommand.throwIfNotQuoted("'fifo'", ""); });
        assertDoesNotThrow(() -> { RequestCommand.throwIfNotQuoted("'door handle'", ""); });
        assertDoesNotThrow(() -> { RequestCommand.throwIfNotQuoted("''", ""); });

        // Invalid inputs
        assertThrows(IllegalArgumentException.class,
                () -> { RequestCommand.throwIfNotQuoted("'", ""); });
        assertThrows(IllegalArgumentException.class,
                () -> { RequestCommand.throwIfNotQuoted("'door", ""); });
        assertThrows(IllegalArgumentException.class,
                () -> { RequestCommand.throwIfNotQuoted("request", ""); });
        assertThrows(IllegalArgumentException.class,
                () -> { RequestCommand.throwIfNotQuoted("", ""); });
    }

    @Test
    public void test_removeQuotes() {
        assertEquals("sjf", RequestCommand.removeQuotes("'sjf'"));
        assertEquals("door handle", RequestCommand.removeQuotes("'door handle'"));
        assertEquals("   ", RequestCommand.removeQuotes("'   '"));
        assertEquals("", RequestCommand.removeQuotes("''"));
    }
}
