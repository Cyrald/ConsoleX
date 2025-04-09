package command;
/**
 * Represents the result of a command execution.
 * Contains output text and error status.
 */
public class CommandResult {
    private final boolean error;
    private final String output;
    
    /**
     * Create a new command result.
     * 
     * @param error Whether the result indicates an error
     * @param output The output text
     */
    public CommandResult(boolean error, String output) {
        this.error = error;
        this.output = output;
    }
    
    /**
     * Create a successful result with output.
     * 
     * @param output The output text
     * @return New CommandResult instance
     */
    public static CommandResult success(String output) {
        return new CommandResult(false, output);
    }
    
    /**
     * Create an error result with message.
     * 
     * @param errorMessage The error message
     * @return New CommandResult instance
     */
    public static CommandResult error(String errorMessage) {
        return new CommandResult(true, errorMessage);
    }
    
    /**
     * Check if the result is an error.
     * 
     * @return true if the result is an error, false otherwise
     */
    public boolean isError() {
        return error;
    }
    
    /**
     * Get the output text.
     * 
     * @return The output text
     */
    public String getOutput() {
        return output;
    }
    
    /**
     * Check if the result has output.
     * 
     * @return true if the result has non-empty output, false otherwise
     */
    public boolean hasOutput() {
        return output != null && !output.trim().isEmpty();
    }
}
