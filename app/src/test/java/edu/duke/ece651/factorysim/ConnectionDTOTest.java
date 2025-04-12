package edu.duke.ece651.factorysim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ConnectionDTOTest {
  @Test
  public void test_connection_dto() {
    ConnectionDTO connection = new ConnectionDTO();
    connection.source = "A";
    connection.destination = "B";
    assertEquals("A", connection.getSource());
    assertEquals("B", connection.getDestination());
  }

  @Test
  public void test_config_data_with_connections() {
    String json = "{\"connections\": [{\"source\": \"A\", \"destination\": \"B\"}]}";
    ConfigData config = new Gson().fromJson(json, ConfigData.class);
    assertNotNull(config.connections);
    assertEquals(1, config.connections.size());
    assertEquals("A", config.connections.get(0).source);
    assertEquals("B", config.connections.get(0).destination);
  }

  @Test
  public void test_establish_connections_method() {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
    ConnectionDTO conn1 = new ConnectionDTO();
    conn1.source = "W";
    conn1.destination = "D";
    ConnectionDTO conn2 = new ConnectionDTO();
    conn2.source = "M";
    conn2.destination = "Hi";
    List<ConnectionDTO> connections = Arrays.asList(conn1, conn2);

    simulation.establishConnections(connections);
    Building woodMine = simulation.getWorld().getBuildingFromName("W");
    Building doorFactory = simulation.getWorld().getBuildingFromName("D");
    Building metalMine = simulation.getWorld().getBuildingFromName("M");
    Building hingeFactory = simulation.getWorld().getBuildingFromName("Hi");

    simulation.addDelivery(woodMine, doorFactory, new Item("wood"), 1);
    simulation.addDelivery(metalMine, hingeFactory, new Item("metal"), 1);
  }

  @Test
  public void test_connections_in_doors_with_connections_json() {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors_with_connections.json");
    World world = simulation.getWorld();

    Building woodMine = world.getBuildingFromName("W");
    Building doorFactory = world.getBuildingFromName("D");
    Building metalMine = world.getBuildingFromName("M");
    Building hingeFactory = world.getBuildingFromName("Hi");
    Building handleFactory = world.getBuildingFromName("Ha");

    // not throw exceptions because they are connected
    simulation.addDelivery(woodMine, doorFactory, new Item("wood"), 1);
    simulation.addDelivery(metalMine, hingeFactory, new Item("metal"), 1);
    simulation.addDelivery(metalMine, handleFactory, new Item("metal"), 1);
    simulation.addDelivery(hingeFactory, doorFactory, new Item("hinge"), 1);
    simulation.addDelivery(handleFactory, doorFactory, new Item("handle"), 1);
  }

  @Test
  public void test_world_builder_build_connections() throws Exception {
    Simulation simulation = new Simulation("src/test/resources/inputs/doors1.json");
    ConnectionDTO conn1 = new ConnectionDTO();
    conn1.source = "W";
    conn1.destination = "D";
    Method buildConnectionsMethod = WorldBuilder.class.getDeclaredMethod(
        "buildConnections", List.class, Simulation.class);
    buildConnectionsMethod.setAccessible(true);
    buildConnectionsMethod.invoke(null, Arrays.asList(conn1), simulation);

    // verify the connections were built
    Building woodMine = simulation.getWorld().getBuildingFromName("W");
    Building doorFactory = simulation.getWorld().getBuildingFromName("D");
    simulation.addDelivery(woodMine, doorFactory, new Item("wood"), 1);

    // test error handling for non-existent buildings
    ConnectionDTO invalidConn = new ConnectionDTO();
    invalidConn.source = "NonExistent";
    invalidConn.destination = "D";
    buildConnectionsMethod.invoke(null, Arrays.asList(invalidConn), simulation);

    // test with world == null scenario
    Simulation nullWorldSim = new Simulation("src/test/resources/inputs/doors1.json") {
      @Override
      public World getWorld() {
        return null;
      }
    };
    buildConnectionsMethod.invoke(null, Arrays.asList(conn1), nullWorldSim);
  }
}
