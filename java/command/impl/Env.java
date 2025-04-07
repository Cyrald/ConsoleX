package command.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import command.Command;
import command.CommandAlias;
import command.CommandResult;

/**
 * Command for working with environment variables.
 */
@CommandAlias({"env"})
public class Env implements Command {
    
    @Override
    public String getName() {
        return "env";
    }
    
    @Override
    public String getDescription() {
        return "Managing environment variables";
    }
    
    @Override
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append("env - Display all environment variables\n");
        usage.append("env list - Display all environment variables\n");
        usage.append("env get <name> - Get the value of an environment variable\n");
        usage.append("env set <name> <value> - Set the value of an environment variable");
        return usage.toString();
    }
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return listEnvironmentVars();
        }
        
        String subCommand = args.get(0).toLowerCase();
        
        switch (subCommand) {
            case "list":
                return listEnvironmentVars();
            case "get":
                if (args.size() < 2) {
                    return new CommandResult(true, "Specify the variable name. Usage: env get <name>");
                }
                return getEnvironmentVar(args.get(1));
            case "set":
                if (args.size() < 3) {
                    return new CommandResult(true, "Not enough arguments. Usage: env set <name> <value>");
                }
                String name = args.get(1);
                StringBuilder valueBuilder = new StringBuilder();
                for (int i = 2; i < args.size(); i++) {
                    if (i > 2) {
                        valueBuilder.append(" ");
                    }
                    valueBuilder.append(args.get(i));
                }
                return setEnvironmentVar(name, valueBuilder.toString());
            default:
                return new CommandResult(true, 
                    "Unknown subcommand: " + subCommand + "\n" + 
                    "Available subcommands: list, get, set");
        }
    }
    
    /**
     * Displays a list of all environment variables
     */
    private CommandResult listEnvironmentVars() {
        Map<String, String> env = System.getenv();
        if (env.isEmpty()) {
            return new CommandResult(false, "No environment variables found");
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Environment variables:\n");
        
        for (Entry<String, String> entry : env.entrySet()) {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        
        return new CommandResult(false, result.toString().trim());
    }
    
    /**
     * Gets the value of an environment variable
     */
    private CommandResult getEnvironmentVar(String name) {
        String value = System.getenv(name);
        
        if (value == null) {
            return new CommandResult(true, "Environment variable not found: " + name);
        }
        
        return new CommandResult(false, value);
    }
    
    /**
     * Sets the value of an environment variable
     * Note: Setting environment variables is not supported by standard means in Java.
     * So the method returns a message about the impossibility of performing the operation.
     */
    private CommandResult setEnvironmentVar(String name, String value) {
        return new CommandResult(true, 
            "Setting environment variables is not supported in Java.\n" +
            "Variable '" + name + "' cannot be set to value '" + value + "'.\n" +
            "Use the 'set' command to set local variables.");
    }
}