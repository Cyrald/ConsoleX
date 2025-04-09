package commandUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages variables for the console application.
 */
public class VariableManager {
    private static final Map<String, String> variables = new HashMap<>();
    private static final Pattern variablePattern = Pattern.compile("\\$(\\w+)|\\$\\{(\\w+)\\}");
    
    /**
     * Private constructor to prevent instantiation.
     */
    private VariableManager() {
    }
    
    /**
     * Set a variable value.
     * 
     * @param name Variable name
     * @param value Variable value
     * @throws IllegalArgumentException if name is null or empty
     */
    public static void setVariable(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        
        // Allow unsetting a variable by setting it to null
        if (value == null) {
            variables.remove(name);
        } else {
            variables.put(name, value);
        }
    }
    
    /**
     * Get a variable value.
     * 
     * @param name Variable name
     * @return The variable value, or null if not set
     */
    public static String getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Get all variables as a map.
     * 
     * @return Map of variable names to values
     */
    public static Map<String, String> getAllVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Process a string and replace variables with their values.
     * Variables can be referenced as $VAR or ${VAR}.
     * 
     * @param input Input string with possible variable references
     * @return Processed string with variables replaced by their values
     */
    public static String processVariables(String input) {
        if (input == null) {
            return null;
        }
        
        Matcher matcher = variablePattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String value = variables.get(varName);
            
            if (value == null) {
                // Leave variable reference unchanged if not defined
                matcher.appendReplacement(result, matcher.group(0));
            } else {
                // Replace variable with its value
                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}