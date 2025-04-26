# FactorySim

A factory simulation application that models production, logistics, and resource management within an industrial environment.

## Overview

FactorySim is a Java-based application that simulates a network of buildings including factories, mines, storage facilities, and drone ports to model resource acquisition, production, and logistics. The simulation allows users to configure buildings, establish connections between them, set policies for request handling and resource sourcing, and observe the flow of materials and production in real-time.

## Features

- **Building Management**: Create and manage different types of buildings, including:
  - Factories for production
  - Mines for resource extraction
  - Storage facilities for inventory management
  - Drone ports for delivery logistics
  - Waste disposal facilities

- **Production Simulation**: Model manufacturing processes with recipes and ingredients
  
- **Resource Management**: Track resources and materials throughout the production chain

- **Logistics System**: Connect buildings with paths and manage deliveries between facilities

- **Drone Delivery System**: Use drones to transport items between buildings

- **Policy Configuration**: Set different policies for request handling and resource sourcing

- **Interactive Command Interface**: Control the simulation through an interactive command-line interface

- **Save/Load Capability**: Save simulation state to JSON files or database

- **Server Integration**: Connect to a server to load presets or user saves

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle build tool

### Building the Project

```bash
./gradlew build
```

### Running the Application

There are three ways to run the application:

1. **Load from a local file**:
```bash
./gradlew run --args="<file_path>"
```

2. **Load a preset from server**:
```bash
./gradlew run --args="<host> <port> <preset>"
```

3. **Load a user save from server**:
```bash
./gradlew run --args="<host> <port> <username> <password>"
```

## Commands

Once the simulation is running, you can use the following commands:

- `step [n]`: Advance the simulation by n time steps (default: 1)
- `finish`: Run the simulation until all requests are completed
- `request <item> <building>`: Place a request for an item at a specific building
- `connect <source> <destination>`: Connect two buildings
- `disconnect <source> <destination>`: Remove connection between two buildings
- `set <policy_type> <policy_name> <target>`: Set a policy for request handling or resource sourcing
- `save <filename>`: Save the current simulation state to a file
- `load <filename>`: Load a simulation state from a file
- `verbose [level]`: Set the verbosity level of the simulation output
- `db-save <user_id>`: Save the simulation state to the database
- `db-load <user_id>`: Load a simulation state from the database

## Configuration

The simulation is configured through JSON files that define:

- Buildings and their properties
- Initial connections between buildings
- Recipes for production
- Resource types
- Map layout and dimensions

## Technologies Used

- Java 21
- Gradle for build management
- JUnit for testing
- Google GSON for JSON handling
- SQLite for database operations

## License

This project is licensed under the terms of the included LICENSE file.

## Development

### Testing

Run the tests with:

```bash
./gradlew test
```

### Code Coverage

Generate code coverage reports with:

```bash
./gradlew cloverGenerateReport
```

The report will be available in `app/build/reports/clover/`
