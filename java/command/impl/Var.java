package command.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.VariableManager;

/**
 * Command to manage variables (set, get, list, clear).
 * This command combines the functionality of Set, Get, and ListVars commands.
 */
@CommandAlias({"var"})
public class Var implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("Usage: " + getUsage());
        }
        
        String operation = args.get(0).toLowerCase();
        
        switch (operation) {
            case "set":
                return handleSet(args.subList(1, args.size()));
            case "get":
                return handleGet(args.subList(1, args.size()));
            case "list":
                return handleList();
            case "clear":
                return handleClear();
            default:
                return CommandResult.error("Unknown operation: " + operation + 
                                         "\nAvailable operations: set, get, list, clear");
        }
    }
    
    /**
     * Handle the 'set' operation.
     * 
     * @param args Arguments for the set operation
     * @return Command result
     */
    private CommandResult handleSet(List<String> args) {
        if (args.size() < 2) {
            return CommandResult.error("Usage: var set <name> <value>");
        }
        
        String name = args.get(0);
        
        // Join remaining arguments to form the value
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 1; i < args.size(); i++) {
            if (i > 1) {
                valueBuilder.append(" ");
            }
            valueBuilder.append(args.get(i));
        }
        
        String value = valueBuilder.toString();
        
        // Process variables in the value
        value = VariableManager.processVariables(value);
        
        try {
            VariableManager.setVariable(name, value);
            return CommandResult.success("Variable set: " + name + " = " + value);
        } catch (IllegalArgumentException e) {
            return CommandResult.error(e.getMessage());
        }
    }
    
    /**
     * Handle the 'get' operation.
     * 
     * @param args Arguments for the get operation
     * @return Command result
     */
    private CommandResult handleGet(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("Usage: var get <name>");
        }
        
        String name = args.get(0);
        String value = VariableManager.getVariable(name);
        
        if (value == null) {
            return CommandResult.error("Variable not found: " + name);
        }
        
        return CommandResult.success(value);
    }
    
    /**
     * Handle the 'list' operation.
     * 
     * @return Command result
     */
    private CommandResult handleList() {
        Map<String, String> variables = VariableManager.getAllVariables();
        
        if (variables.isEmpty()) {
            return CommandResult.success("No variables defined.");
        }
        
        // Sort variables by name
        Map<String, String> sortedVars = new TreeMap<>(variables);
        
        StringBuilder result = new StringBuilder("Variables:\n\n");
        
        for (Map.Entry<String, String> entry : sortedVars.entrySet()) {
            result.append(String.format("%-15s = %s\n", entry.getKey(), entry.getValue()));
        }
        
        return CommandResult.success(result.toString());
    }
    
    /**
     * Handle the 'clear' operation.
     * 
     * @return Command result
     */
    private CommandResult handleClear() {
        Map<String, String> variables = VariableManager.getAllVariables();
        int count = variables.size();
        
        if (count == 0) {
            return CommandResult.success("No variables to clear.");
        }
        
        // Clear all variables
        for (String name : variables.keySet()) {
            VariableManager.setVariable(name, null);
        }
        
        return CommandResult.success("Cleared " + count + " variables.");
    }
    
    @Override
    public String getName() {
        return "var";
    }
    
    @Override
    public String getDescription() {
        return "Manages variables (set, get, list, clear).";
    }
    
    @Override
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append("var set <name> <value> - Set a variable value\n");
        usage.append("var get <name> - Get a variable value\n");
        usage.append("var list - List all variables\n");
        usage.append("var clear - Clear all variables");
        return usage.toString();
    }
}