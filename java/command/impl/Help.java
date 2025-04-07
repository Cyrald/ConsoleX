package command.impl;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import command.Command;
import command.CommandAlias;
import command.CommandExecutor;
import command.CommandResult;

/**
 * Command for displaying help information about available commands.
 */
@CommandAlias({"help"})
public class Help implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        StringBuilder output = new StringBuilder();
        CommandExecutor executor = new CommandExecutor();
        
        if (args.isEmpty()) {
            // Show help for all commands
            output.append("Available commands:\n\n");
            
            // Get all main commands (excluding aliases)
            Set<Command> uniqueCommands = new TreeSet<>((c1, c2) -> 
                c1.getName().compareTo(c2.getName()));
            uniqueCommands.addAll(executor.getCommands().values());
            
            for (Command command : uniqueCommands) {
                output.append(command.getName())
                      .append(" - ")
                      .append(command.getDescription())
                      .append("\n");
                
                // Show aliases if exist
                getAliases(command).ifPresent(aliases -> {
                    output.append("   Aliases: ")
                          .append(String.join(", ", aliases))
                          .append("\n");
                });
            }
            
            output.append("\nEnter 'help <command>' for more detailed information about a specific command.");
        } else {
            // Show help for a specific command
            String commandName = args.get(0).toLowerCase();
            Command command = executor.getCommand(commandName);
            
            if (command == null) {
                return CommandResult.error("Unknown command: " + commandName);
            }
            
            output.append("Help for command '")
                  .append(command.getName())
                  .append("':\n\n");
            
            output.append("Description: ")
                  .append(command.getDescription())
                  .append("\n\n");
            
            output.append("Usage: ")
                  .append(command.getUsage())
                  .append("\n");
            
            // Show aliases if exist
            getAliases(command).ifPresent(aliases -> {
                output.append("\nAliases: ")
                      .append(String.join(", ", aliases))
                      .append("\n");
            });
        }
        
        return CommandResult.success(output.toString());
    }
    
    /**
     * Get aliases for a command using reflection.
     * 
     * @param command Command for which to get aliases
     * @return Set of aliases or empty optional if not found
     */
    private java.util.Optional<Set<String>> getAliases(Command command) {
        try {
            Class<?> commandClass = command.getClass();
            if (commandClass.isAnnotationPresent(CommandAlias.class)) {
                CommandAlias aliasAnnotation = commandClass.getAnnotation(CommandAlias.class);
                Set<String> aliases = new TreeSet<>(List.of(aliasAnnotation.value()));
                return java.util.Optional.of(aliases);
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        return java.util.Optional.empty();
    }
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "Displays help information about available commands";
    }
    
    @Override
    public String getUsage() {
        return "help [command]";
    }
}