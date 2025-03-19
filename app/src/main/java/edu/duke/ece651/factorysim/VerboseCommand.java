package edu.duke.ece651.factorysim;

public class VerboseCommand implements Command {
  @Override
  public String getName() {
    return "verbose";
  }

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
    try {
      sim.setVerbosity(n);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid 'verbose' command: bad verbosity range", e);
    }
  }
}
