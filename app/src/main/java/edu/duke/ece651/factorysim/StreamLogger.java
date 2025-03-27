package edu.duke.ece651.factorysim;

import java.io.*;

/**
 * Represents a logger that logs to an output stream.
 */
public class StreamLogger implements Logger {
  private final PrintWriter writer;

  /**
   * Constructs a stream logger.
   * 
   * @param stream is the output stream of the logger.
   */
  public StreamLogger(OutputStream stream) {
    this.writer = new PrintWriter(stream, true);
  }

  /**
   * Logs to the stream.
   * 
   * @param s is the string to be logged to stream.
   */
  @Override
  public void log(String s) {
    writer.println(s);
  }
}
