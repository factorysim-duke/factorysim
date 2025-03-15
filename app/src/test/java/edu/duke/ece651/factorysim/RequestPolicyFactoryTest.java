package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RequestPolicyFactoryTest {
  @Test
  public void test_createPolicy() {
    RequestPolicy fifo = RequestPolicyFactory.createPolicy("fifo");
    RequestPolicy ready = RequestPolicyFactory.createPolicy("ready");
    RequestPolicy sjf = RequestPolicyFactory.createPolicy("sjf");
    assertEquals(fifo.getClass(), FifoRequestPolicy.class);
    assertEquals(ready.getClass(), ReadyRequestPolicy.class);
    assertThrows(IllegalArgumentException.class, () -> RequestPolicyFactory.createPolicy("unknown"));
  }

}
