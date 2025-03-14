package edu.duke.ece651.factorysim;

import java.util.Arrays;

/**
 * Handles the "set policy" command.
 */
public class SetPolicyCommand implements Command {
    private static final String [] VALID_TYPES = {"request", "source"};
    private static final String [] VALID_REQUEST_POLICIES = {"fifo", "sjf", "ready", "default"};
    private static final String [] VALID_SOURCE_POLICIES = {"default", "qlen", "simplelat", "recursivelat"};
    private static final String [] VALID_SPECIAL_TARGETS = {"*", "default"};
    /**
     * Returns the name of the command.
     * @return the name of the command, "set"
     */
    @Override
    public String getName() {
        return "set";
    }

    /**
     * Execute the command. First check if the syntax is valid.
     * Then, set the policy for the given type and target.
     * @param args the arguments of the command
     * @param sim the simulation
     */
    @Override
    public void execute(String[] args, Simulation sim) {
        if (args.length != 5 || !args[1].equals("policy") || !args[3].equals("on")) {
            throw new IllegalArgumentException("Invalid syntax. Use: set policy TYPE POLICY on TARGET");
        }

        String type = args[2];  // "request" or "source"
        String policy = args[3]; // the policy to be set
        String target = args[4]; // Building name, "*" (all buildings), or "default" (all buildings except user)

        // Check if the type is valid
        if (!Utils.isInList(type, VALID_TYPES)) {
            throw new IllegalArgumentException("TYPE must be either 'request' or 'source'");
        }

        // Check if the policy is valid
        if (type.equals("request") && !Utils.isInList(policy, VALID_REQUEST_POLICIES)) {
            throw new IllegalArgumentException("POLICY must be either 'fifo', 'sjf', or 'qlen'");
        }
        if (type.equals("source") && !Utils.isInList(policy, VALID_SOURCE_POLICIES)) {
            throw new IllegalArgumentException("POLICY must be either 'default', 'qlen', 'simplelat', or 'recursivelat'");
        }

        // Check if the target is valid
        if (!Utils.isInList(target, VALID_SPECIAL_TARGETS) && !isQuoted(target)) {
            throw new IllegalArgumentException("TARGET must be a building name, '*', or 'default'");
        }

        sim.setPolicy(type, policy, target);
    }
}