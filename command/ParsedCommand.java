package command;

import java.util.List;

/**
 * Represents a parsed command with name and arguments.
 */
public class ParsedCommand {
    private final String commandName;
    private final List<String> args;
    
    /**
     * Create a new parsed command.
     * 
     * @param commandName Command name
     * @param args Command arguments
     */
    public ParsedCommand(String commandName, List<String> args) {
        this.commandName = commandName;
        this.args = args;
    }
    
    /**
     * Get the command name.
     * 
     * @return Command name
     */
    public String getCommandName() {
        return commandName;
    }
    
    /**
     * Get the command arguments.
     * 
     * @return Command arguments
     */
    public List<String> getArgs() {
        return args;
    }
    
    /**
     * String representation of the parsed command.
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return commandName + " " + String.join(" ", args);
    }
}
