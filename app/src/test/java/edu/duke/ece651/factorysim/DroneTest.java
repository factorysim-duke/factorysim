package edu.duke.ece651.factorysim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DroneTest {
  @Test
  void test_drone_methods() {
    Drone drone = new Drone();
    assertFalse(drone.isInUse());
    drone.setInUse(true);
    assertTrue(drone.isInUse());
    drone.setInUse(false);
    assertFalse(drone.isInUse());
    assertEquals(5, Drone.getSpeed());
    drone.setInUse(true);
    assertEquals(true, drone.toJson().get("inUse").getAsBoolean());
    drone.setInUse(false);
    assertEquals(false, drone.toJson().get("inUse").getAsBoolean());
  }
}
