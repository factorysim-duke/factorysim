package edu.duke.ece651.factorysim;

import java.util.*;

/**
 * A class that handles commands operating on the simulation instance.<br/>
 *
 * NOTE: To add new commands, add another `registerCommand` call in `initCommandMap` with the new `Command` instance.
 */
public class CommandHandler {
    private final HashMap<String, Command> commandMap = new HashMap<>();

    /**
     * Initialize the command map with default commands.
     */
    private void initCommandMap() {
        commandMap.clear();
        registerCommand(new RequestCommand());
        registerCommand(new StepCommand());
        registerCommand(new FinishCommand());
        registerCommand(new SetPolicyCommand());
        registerCommand(new VerboseCommand());
        registerCommand(new SaveCommand());
        registerCommand(new LoadCommand());
    }

    /**
     * Register a command to the command map.
     *
     * @param command `Command` instance to register.
     */
    private void registerCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    private final Simulation sim;

    /**
     * Constructs a `CommandHandler` instance from an injected `Simulation` instance.
     *
     * @param sim injected `Simulation` instance to operate on.
     */
    public CommandHandler(Simulation sim) {
        this.sim = sim;
        initCommandMap();
    }

    /**
     * Execute with the string line as the command.<br/>
     * NOTE: The method assumes the whole string is the command, meaning it won't stop at the first newline character.
     *
     * @param line line of command to execute.
     * @throws IllegalArgumentException when the command is illegal.
     */
    public void execute(String line) {
        // Split the line into arguments
        String[] args = parseCommand(line);
        if (args.length == 0) {
            throw new IllegalArgumentException("Invalid command: empty arguments");
        }

        // Execute command based on the command map
        String name = args[0];
        if (commandMap.containsKey(name)) {
            commandMap.get(name).execute(args, sim);
        } else {
            throw new IllegalArgumentException("Invalid command: unknown command '" + name + "'");
        }
    }

    /**
     * Parse a line of command into an array of arguments.<br/>
     * Example:<br/>
     * parse<br/>
     * <code>"request 'bolt' from 'best doors and bolts in town'"</code><br/>
     * into<br/>
     * <code>["request", "'bolt'", "from", "'best doors and bolts in town'"]</code>
     *
     * @param line string line to parse.
     * @return parsed arguments.
     * @throws IllegalArgumentException when command is invalid.
     */
    static String[] parseCommand(String line) {
        ArrayList<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            // Ignore whitespaces
            if (Character.isWhitespace(c)) {
                continue;
            }

            // If quoted argument
            if (c == '\'') {
                // Append opening '
                i++;
                sb.append('\'');

                // Append contents
                for (; i < line.length() && line.charAt(i) != '\''; i++) {
                    sb.append(line.charAt(i));
                }

                // Make sure there's a closing quote
                if (i == line.length()) {
                    throw new IllegalArgumentException("Invalid command: missing closing ' at the end");
                }

                // Append ending '
                sb.append('\'');
            } else {
                // Regular argument
                for (; i < line.length() && !Character.isWhitespace(line.charAt(i)); i++) {
                    sb.append(line.charAt(i));
                }
                i--; // To put i at the character after right current argument in the next outer loop iteration
            }

            // Store stored argument and clear the `StringBuilder`
            args.add(sb.toString());
            sb.setLength(0);
        }
        return args.toArray(new String[0]);
    }
}
