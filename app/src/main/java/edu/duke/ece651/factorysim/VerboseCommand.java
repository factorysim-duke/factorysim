package edu.duke.ece651.factorysim;

/**
 * Represents the verbose command in the simulation.
 */
public class VerboseCommand implements Command {
  /**
   * Gets the name of the command.
   * 
   * @return the name of this verbose command.
   */
  @Override
  public String getName() {
    return "verbose";
  }

  /**
   * Executes the command.
   * 
   * @param args is the list of strings of arguments for the command.
   * @param sim  is the simulation in which to execute command.
   */
  @Override
  public void execute(String[] args, Simulation sim) {
    // Check argument count
    if (args.length != 2) {
      throw new IllegalArgumentException("Invalid 'verbose' command: illegal number of arguments");
    }

    // Make sure the first argument is "verbose"
    if (!args[0].equals(getName())) {
      throw new IllegalArgumentException("Invalid 'verbose' command: command name doesn't match");
    }

    // Parse steps
    int n;
    try {
      n = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid 'verbose' command: bad `N` integer format", e);
    }

    // Operate on the simulation
    sim.setVerbosity(n);
  }
}
