package command;
import java.util.List;

/**
 * Interface for all console commands.
 * All command implementations must implement this interface.
 */
public interface Command {
    
    /**
     * Execute the command with the given arguments.
     * 
     * @param args Command arguments
     * @return Result of the command execution
     */
    CommandResult execute(List<String> args);
    
    /**
     * Get the name of the command.
     * This is the primary identifier used to invoke the command.
     * 
     * @return Command name
     */
    String getName();
    
    /**
     * Get a brief description of what the command does.
     * Used in help text.
     * 
     * @return Command description
     */
    String getDescription();
    
    /**
     * Get usage information for the command.
     * Used in help text to show how to use the command.
     * 
     * @return Command usage information
     */
    String getUsage();
}
