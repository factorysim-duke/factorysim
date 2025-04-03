package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SourcePolicyFactoryTest {
  @Test
  public void test_createPolicy() {
    SourcePolicy qlen = SourcePolicyFactory.createPolicy("qlen");
    SourcePolicy simplelat = SourcePolicyFactory.createPolicy("simplelat");
    SourcePolicy recursivelat = SourcePolicyFactory.createPolicy("recursivelat");
    assertEquals(qlen.getClass(), QLenSourcePolicy.class);
    assertEquals(simplelat.getClass(), SimpleLatSourcePolicy.class);
    assertEquals(recursivelat.getClass(), RecursiveLatSourcePolicy.class);
    assertThrows(IllegalArgumentException.class, () -> SourcePolicyFactory.createPolicy("unknown"));
  }

}
