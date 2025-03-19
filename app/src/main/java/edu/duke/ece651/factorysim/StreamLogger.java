package edu.duke.ece651.factorysim;

import java.io.*;

/**
 * Represents a logger that logs to an output stream.
 */
public class StreamLogger implements Logger {
  private final PrintWriter writer;

  public StreamLogger(OutputStream stream) {
    this.writer = new PrintWriter(stream, true);
  }

  @Override
  public void log(String s) {
    writer.println(s);
  }
}
