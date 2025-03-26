package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SetPolicyCommandTest {
  SetPolicyCommand command = new SetPolicyCommand();

  @Test
  public void test_execute_request() {
    command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "'D'" }, new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_execute_source() {
    command.execute(new String[] { "set", "policy", "source", "'qlen'", "on", "'D'" }, new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_invalid_syntax() {
    assertThrows(IllegalArgumentException.class, () -> command
        .execute(new String[] { "set", "policy", "request", "'fifo'", "on" }, new TestUtils.MockSimulation()));
  }

  @Test
  public void test_invalid_type() {
    assertThrows(IllegalArgumentException.class, () -> command
        .execute(new String[] { "set", "policy", "unknown", "'fifo'", "on", "'D'" }, new TestUtils.MockSimulation()));
  }

  @Test
  public void test_invalid_policy_request() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "request", "'unknown'", "on", "'D'" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_invalid_policy_source() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "source", "'unknown'", "on", "'D'" },
            new TestUtils.MockSimulation()));
  }
  
  @Test
  public void test_unquoted_policy_request() {
    command.execute(new String[] { "set", "policy", "request", "default", "on", "'D'" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_unquoted_policy_source() {
    command.execute(new String[] { "set", "policy", "source", "default", "on", "'D'" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_invalid_target_request() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "unknown" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_invalid_target_source() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "source", "'qlen'", "on", "unknown" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_unknown_target() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "'unknown'" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_unquoted_target_request() {
    command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "*" }, new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_unquoted_target_source() {
    command.execute(new String[] { "set", "policy", "source", "'qlen'", "on", "*" }, new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_fifo_on_default_request_policy() {
    command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "default" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_qlen_on_default_source_policy() {
    command.execute(new String[] { "set", "policy", "source", "'qlen'", "on", "default" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_default_policy_on_building() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "request", "default", "on", "'unknown'" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_default_policy_on_all_buildings() {
    command.execute(new String[] { "set", "policy", "request", "default", "on", "*" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_all_target() {
    command.execute(new String[] { "set", "policy", "request", "'fifo'", "on", "*" },
        new TestUtils.MockSimulation());
    assertEquals(command.getClass(), SetPolicyCommand.class);
  }

  @Test
  public void test_quoted_target() {
    assertThrows(IllegalArgumentException.class,
        () -> command.execute(new String[] { "set", "policy", "request", "default", "on", "default" },
            new TestUtils.MockSimulation()));
  }

  @Test
  public void test_execute_throws() {
    assertThrows(IllegalArgumentException.class,
            () -> command.execute(new String[] { "sat", "policy", "request", "'fifo'", "on", "*" },
                    new TestUtils.MockSimulation()));

    assertThrows(IllegalArgumentException.class,
            () -> command.execute(new String[] { "set", "bolicy", "request", "'fifo'", "in", "*" },
                    new TestUtils.MockSimulation()));

    assertThrows(IllegalArgumentException.class,
            () -> command.execute(new String[] { "set", "policy", "request", "'fifo'", "in", "*" },
                    new TestUtils.MockSimulation()));
  }
}
