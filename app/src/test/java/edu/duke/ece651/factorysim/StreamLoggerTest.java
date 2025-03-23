package edu.duke.ece651.factorysim;

import java.io.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StreamLoggerTest {
  private void assertLog(String... logs) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Logger logger = new StreamLogger(stream);

    StringBuilder expected = new StringBuilder();
    for (String log : logs) {
      logger.log(log);
      expected.append(log);
      expected.append(System.lineSeparator());
    }

    assertEquals(expected.toString(), stream.toString());
  }

  @Test
  public void test_log() {
    assertLog("[order complete] Order 0 completed (door) at time 21");

    assertLog("[order complete] Order 0 completed (door) at time 21",
              "Simulation completed at time-step 50");

    assertLog("[ingredient assignment]: wood assigned to W to deliver to D",
              "[ingredient assignment]: handle assigned to Ha to deliver to D",
              "[ingredient assignment]: metal assigned to M to deliver to Ha",
              "[ingredient assignment]: hinge assigned to Hi to deliver to D",
              "[ingredient assignment]: metal assigned to M to deliver to Hi",
              "[ingredient assignment]: hinge assigned to Hi to deliver to D",
              "[ingredient assignment]: metal assigned to M to deliver to Hi",
              "[ingredient assignment]: hinge assigned to Hi to deliver to D",
              "[ingredient assignment]: metal assigned to M to deliver to Hi");

    assertLog(
            "[source selection]: D (qlen) has request for door on 0",
            "[D:door:0] For ingredient wood",
            "    W: 0",
            "    Selecting W",
            "[ingredient assignment]: wood assigned to W to deliver to D",
            "[D:door:1] For ingredient handle",
            "    Ha: 0",
            "    Selecting Ha",
            "[ingredient assignment]: handle assigned to Ha to deliver to D",
            "[source selection]: Ha (qlen) has request for handle on 0",
            "[Ha:handle:0] For ingredient metal",
            "    M: 0",
            "    Selecting M",
            "[ingredient assignment]: metal assigned to M to deliver to Ha",
            "[D:door:2] For ingredient hinge",
            "    Hi: 0",
            "    Selecting Hi",
            "[ingredient assignment]: hinge assigned to Hi to deliver to D",
            "[source selection]: Hi (qlen) has request for hinge on 0",
            "[Hi:hinge:0] For ingredient metal",
            "    M: 1",
            "    Selecting M",
            "[ingredient assignment]: metal assigned to M to deliver to Hi",
            "    Hi: 1",
            "    Selecting Hi",
            "[ingredient assignment]: hinge assigned to Hi to deliver to D",
            "[source selection]: Hi (qlen) has request for hinge on 0",
            "[Hi:hinge:0] For ingredient metal",
            "    M: 2",
            "    Selecting M",
            "[ingredient assignment]: metal assigned to M to deliver to Hi",
            "    Hi: 2",
            "    Selecting Hi",
            "[ingredient assignment]: hinge assigned to Hi to deliver to D",
            "[source selection]: Hi (qlen) has request for hinge on 0",
            "[Hi:hinge:0] For ingredient metal",
            "    M: 3",
            "    Selecting M",
            "[ingredient assignment]: metal assigned to M to deliver to Hi"
    );

    assertLog();
  }
}
