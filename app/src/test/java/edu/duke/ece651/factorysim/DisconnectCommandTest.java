package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DisconnectCommandTest {

    @Test
    public void testExecute_invalidArgCount() {
        DisconnectCommand cmd = new DisconnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"disconnect", "'D'", "to"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("illegal number of arguments"));

        String[] args2 = {"disconnect", "'D'", "to", "'W'", "extra"};
        Exception ex2 = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args2, sim);
        });
        assertTrue(ex2.getMessage().contains("illegal number of arguments"));
    }

    @Test
    public void testExecute_commandNameMismatch() {
        DisconnectCommand cmd = new DisconnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"notdisconnect", "'D'", "to", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("command name doesn't match"));
    }

    @Test
    public void testExecute_invalidSyntax() {
        DisconnectCommand cmd = new DisconnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"disconnect", "'D'", "from", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("invalid syntax"));
    }

    @Test
    public void testExecute_secondArgNotQuoted() {
        DisconnectCommand cmd = new DisconnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"disconnect", "D", "to", "'W'"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("2nd argument must be a quoted name"));
    }

    @Test
    public void testExecute_fourthArgNotQuoted() {
        DisconnectCommand cmd = new DisconnectCommand();
        Simulation sim = new TestUtils.MockSimulation();

        String[] args = {"disconnect", "'D'", "to", "W"};
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            cmd.execute(args, sim);
        });
        assertTrue(ex.getMessage().contains("4th argument must be a quoted name"));
    }

    @Test
    public void testExecute_validDisconnectCommand() {
        class DummySimulation extends TestUtils.MockSimulation {
            public boolean disconnectCalled = false;
            public String srcArg = null;
            public String dstArg = null;

            @Override
            public boolean disconnectBuildings(String source, String dest) {
                disconnectCalled = true;
                srcArg = source;
                dstArg = dest;
                return true;
            }
        }

        DummySimulation sim = new DummySimulation();
        sim.connectBuildings("D", "W");
        DisconnectCommand cmd = new DisconnectCommand();
        String[] args = {"disconnect", "'D'", "to", "'W'"};
        cmd.execute(args, sim);

        assertTrue(sim.disconnectCalled);
        assertEquals("D", sim.srcArg);
        assertEquals("W", sim.dstArg);
    }

}
