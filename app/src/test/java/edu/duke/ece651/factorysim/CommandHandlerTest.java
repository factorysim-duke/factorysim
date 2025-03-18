package edu.duke.ece651.factorysim;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandHandlerTest {
    @Test
    public void test_execute() {
        Simulation sim = new Simulation("src/test/resources/inputs/doors1.json");
        CommandHandler commandHandler = new CommandHandler(sim);

        // Invalid commands
        assertThrows(IllegalArgumentException.class, () -> { commandHandler.execute(""); });
        assertThrows(IllegalArgumentException.class, () -> { commandHandler.execute("   "); });
        assertThrows(IllegalArgumentException.class, () -> { commandHandler.execute("unknown 1 2 'A B C'"); });

        // Valid commands
        assertDoesNotThrow(() -> { commandHandler.execute("request 'door' from 'D'"); });
        assertDoesNotThrow(() -> { commandHandler.execute("step 3"); });
        assertDoesNotThrow(() -> { commandHandler.execute("finish"); });
    }

    @Test
    public void test_parseCommand() {
        // Valid commands
        assert_parseCommand("request 'ITEM' from 'BUILDING'",
                List.of("request", "'ITEM'", "from", "'BUILDING'"));
        assert_parseCommand("request 'bolt' from 'best doors and bolts in town'",
                List.of("request", "'bolt'", "from", "'best doors and bolts in town'"));
        assert_parseCommand("finish", List.of("finish"));
        assert_parseCommand("step 3", List.of("step", "3"));
        assert_parseCommand("set policy request 'sjf' on 'door factory'",
                List.of("set", "policy", "request", "'sjf'", "on", "'door factory'"));
        assert_parseCommand("set policy source 'qlen' on 'door factory'",
                List.of("set", "policy", "source", "'qlen'", "on", "'door factory'"));

        // Empty
        assert_parseCommand("", Collections.emptyList());
        assert_parseCommand("   \t  \r\n\r", Collections.emptyList());

        // Multiple spaces
        assert_parseCommand("   request   'handle'  from        'Ha'     ",
                List.of("request", "'handle'", "from", "'Ha'"));

        // Other whitespace characters
        assert_parseCommand("\tstep\t10\t",
                List.of("step", "10"));

        // Invalid command: missing ending '
        assertThrows(IllegalArgumentException.class,
                () -> { CommandHandler.parseCommand("request 'door' from 'door factory"); });
    }

    private static void assert_parseCommand(String command, Iterable<String> expected) {
        assertIterableEquals(expected, Arrays.asList(CommandHandler.parseCommand(command)));
    }
}
