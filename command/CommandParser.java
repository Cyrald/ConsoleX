package command;

import java.util.ArrayList;
import java.util.List;

import command.impl.Alias;
import commandUtils.VariableManager;

/**
 * Parser for command input.
 * Handles nested commands and argument parsing with unlimited nesting.
 */
public class CommandParser {
    private CommandExecutor commandExecutor;
    
    // Flag to prevent stack overflow during command substitution
    private static ThreadLocal<Boolean> processingCommand = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    public CommandParser() {
    }

    /**
     * Set the command executor reference.
     * 
     * @param commandExecutor Reference to the command executor
     */
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
    
    /**
     * Parse a command string into a ParsedCommand object.
     * 
     * @param input Command string to parse
     * @return ParsedCommand object or null if input is empty
     */
    public ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        // First, we need to expand any command substitutions if not already processing
        if (!processingCommand.get()) {
            processingCommand.set(true);
            try {
                input = expandCommandSubstitutions(input);
            } finally {
                processingCommand.set(false);
            }
        }
        
        // Then process variable substitution
        input = VariableManager.processVariables(input);
        
        // Tokenize the expanded input
        List<String> tokens = tokenize(input);
        
        if (tokens.isEmpty()) {
            return null;
        }
        
        String commandName = tokens.get(0);
        List<String> args = tokens.size() > 1 ? tokens.subList(1, tokens.size()) : new ArrayList<>();
        
        // Check if this is an alias and resolve it if needed
        if (Alias.isAlias(commandName)) {
            return resolveAlias(commandName, args);
        }
        
        return new ParsedCommand(commandName, args);
    }
    
    /**
     * Resolve an alias to its actual command.
     * 
     * @param aliasName The name of the alias
     * @param originalArgs Arguments that were provided to the alias
     * @return ParsedCommand object for the resolved command
     */
    private ParsedCommand resolveAlias(String aliasName, List<String> originalArgs) {
        String aliasCommand = Alias.getAliasCommand(aliasName);
        
        if (aliasCommand == null) {
            // This shouldn't happen if isAlias returned true, but just in case
            return new ParsedCommand(aliasName, originalArgs);
        }
        
        // Parse the alias command
        List<String> aliasTokens = tokenize(aliasCommand);
        if (aliasTokens.isEmpty()) {
            // Empty alias, return original
            return new ParsedCommand(aliasName, originalArgs);
        }
        
        // Get the actual command name
        String actualCommandName = aliasTokens.get(0);
        
        // Combine alias arguments with the original arguments
        List<String> combinedArgs = new ArrayList<>();
        if (aliasTokens.size() > 1) {
            combinedArgs.addAll(aliasTokens.subList(1, aliasTokens.size()));
        }
        combinedArgs.addAll(originalArgs);
        
        // Check if the resolved command is itself an alias (prevent infinite loops)
        if (Alias.isAlias(actualCommandName) && !actualCommandName.equals(aliasName)) {
            // Only recurse if it's a different alias to prevent infinite loops
            return resolveAlias(actualCommandName, combinedArgs);
        }
        
        return new ParsedCommand(actualCommandName, combinedArgs);
    }
    
    /**
     * Find the innermost command substitution in a string.
     * 
     * @param input The string to search
     * @return int[] with {startIndex, endIndex} or null if none found
     */
    private int[] findInnermostSubstitution(String input) {
        // This method finds a $(command) expression with no nested $( inside it
        for (int i = 0; i < input.length() - 1; i++) {
            if (input.charAt(i) == '$' && i + 1 < input.length() && input.charAt(i + 1) == '(') {
                int startPos = i;
                int openParens = 1;
                boolean hasNestedCommand = false;
                
                for (int j = i + 2; j < input.length(); j++) {
                    if (input.charAt(j) == '$' && j + 1 < input.length() && input.charAt(j + 1) == '(') {
                        openParens++;
                        hasNestedCommand = true;
                    } else if (input.charAt(j) == ')') {
                        openParens--;
                        if (openParens == 0) {
                            // Found a complete command
                            if (!hasNestedCommand) {
                                // This is an innermost command without nested commands
                                return new int[] { startPos, j };
                            } else {
                                // This has nested commands, keep searching
                                break;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Expands all command substitutions in the format $(command) with unlimited nesting depth.
     * 
     * @param input Input string with potential command substitutions
     * @return String with all command substitutions expanded
     */
    private String expandCommandSubstitutions(String input) {
        if (commandExecutor == null || input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder(input);
        boolean foundSubstitution;
        
        do {
            foundSubstitution = false;
            
            // Find any innermost command substitution
            int[] indices = findInnermostSubstitution(result.toString());
            
            if (indices != null) {
                int startIndex = indices[0];
                int endIndex = indices[1];
                
                // Extract the command without the $( and )
                String cmdContent = result.substring(startIndex + 2, endIndex);
                
                // Create temporary command parser to avoid stack overflow
                CommandParser tempParser = new CommandParser();
                tempParser.setCommandExecutor(commandExecutor);
                
                // Parse and execute the command
                ParsedCommand command = tempParser.parse(cmdContent);
                String output = "";
                
                if (command != null) {
                    CommandResult cmdResult = commandExecutor.execute(command, null);
                    if (cmdResult != null) {
                        output = cmdResult.getOutput();
                        
                        // If there was an error, indicate it
                        if (cmdResult.isError()) {
                            output = "ERROR: " + output;
                        }
                    }
                }
                
                // Replace the command substitution with its output
                result.replace(startIndex, endIndex + 1, output);
                foundSubstitution = true;
            }
        } while (foundSubstitution);
        
        return result.toString();
    }
    
    /**
     * Tokenize a command string into a list of tokens, respecting quoted strings.
     * 
     * @param input Command string to tokenize
     * @return List of tokens
     */
    public List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // Handle escape character
            if (c == '\\' && i + 1 < input.length()) {
                char nextChar = input.charAt(i + 1);
                if (nextChar == '"' || nextChar == '\'' || nextChar == '\\' || nextChar == ' ') {
                    currentToken.append(nextChar);
                    i++; // Skip the escaped character
                    continue;
                }
            }
            
            // Handle quotes
            if (c == '"' || c == '\'') {
                if (inQuotes) {
                    if (c == quoteChar) {
                        // End of quoted section
                        inQuotes = false;
                        quoteChar = 0;
                    } else {
                        // Add the quote character itself
                        currentToken.append(c);
                    }
                } else {
                    inQuotes = true;
                    quoteChar = c;
                }
                continue;
            }
            
            // Handle spaces (token separators)
            if (c == ' ' && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                continue;
            }
            
            // Add regular character
            currentToken.append(c);
        }
        
        // Add the last token if any
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        
        return tokens;
    }
}
