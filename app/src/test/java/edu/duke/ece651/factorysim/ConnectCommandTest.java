package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ConnectCommandTest {

    @Test
    public void testExecute_invalidArgCount() {
        ConnectCommand cmd = new ConnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"connect", "'D'", "to"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("illegal number of arguments"));

        String[] args2 = {"connect", "'D'", "to", "'W'", "extra"};
        Exception ex2 = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args2, sim);
        });
        assertTrue(ex2.getMessage().contains("illegal number of arguments"));
    }

    @Test
    public void testExecute_commandNameMismatch() {
        ConnectCommand cmd = new ConnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"notconnect", "'D'", "to", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("command name doesn't match"));
    }

    @Test
    public void testExecute_invalidSyntax() {
        ConnectCommand cmd = new ConnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"connect", "'D'", "from", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("invalid syntax"));
    }

    @Test
    public void testExecute_secondArgNotQuoted() {
        ConnectCommand cmd = new ConnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"connect", "D", "to", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("2nd argument must be a quoted name"));
    }

    @Test
    public void testExecute_fourthArgNotQuoted() {
        ConnectCommand cmd = new ConnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"connect", "'D'", "to", "W"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("4th argument must be a quoted name"));
    }

    @Test
    public void testExecute_validCommand() {
        class DummySimulation extends TestUtils.MockSimulation {
            public boolean connectCalled = false;
            public String srcArg = null;
            public String dstArg = null;

            @Override
            public boolean connectBuildings(String source, String dest) {
                connectCalled = true;
                srcArg = source;
                dstArg = dest;
                return true;
            }
        }

        DummySimulation sim = new DummySimulation();
        ConnectCommand cmd = new ConnectCommand();
        String[] args = {"connect", "'D'", "to", "'W'"};
        cmd.execute(args, sim);

        assertTrue(sim.connectCalled);
        assertEquals("D", sim.srcArg);
        assertEquals("W", sim.dstArg);
    }
}
