package edu.duke.ece651.factorysim;

/*
 * ConnectionDTO is a data transfer object for reading Connection JSON data.
 * It is used to transfer Connection JSON data to building connection objects.
 */
public class ConnectionDTO {
  public String source;
  public String destination;

  /**
   * Default constructor for JSON deserialization.
   */
  public ConnectionDTO() {
  }
  
  /**
   * Constructor for creating a ConnectionDTO with source and destination.
   * 
   * @param source The source building name
   * @param destination The destination building name
   */
  public ConnectionDTO(String source, String destination) {
    this.source = source;
    this.destination = destination;
  }

  /**
   * Gets the source building name for a connection.
   * 
   * @return the name of the source building.
   */
  public String getSource() {
    return source;
  }

  /**
   * Gets the destination building name for a connection.
   * 
   * @return the name of the destination building.
   */
  public String getDestination() {
    return destination;
  }
} 