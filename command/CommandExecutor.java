package command;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import ui.ConsoleUI;

/**
 * Responsible for discovering and executing commands.
 * Uses reflection to find command implementations.
 */
public class CommandExecutor {
    private final Map<String, Command> commandMap = new HashMap<>();
    
    /**
     * Constructor that discovers all command implementations
     * and registers them along with their aliases.
     */
    public CommandExecutor() {
        discoverCommands();
    }
    
    /**
     * Use reflection to find all classes that implement the Command interface
     * and register them along with their aliases.
     */
    private void discoverCommands() {
        try {
            Reflections reflections = new Reflections("command.impl");
            Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
            
            for (Class<? extends Command> commandClass : commandClasses) {
                // Skip abstract classes and interfaces
                if (Modifier.isAbstract(commandClass.getModifiers()) || 
                    commandClass.isInterface()) {
                    continue;
                }
                
                // Create instance of the command
                Command command = commandClass.getDeclaredConstructor().newInstance();
                
                // Register the command with its primary name
                commandMap.put(command.getName().toLowerCase(), command);
                
                // Register command aliases if present
                if (commandClass.isAnnotationPresent(CommandAlias.class)) {
                    CommandAlias aliases = commandClass.getAnnotation(CommandAlias.class);
                    for (String alias : aliases.value()) {
                        commandMap.put(alias.toLowerCase(), command);
                    }
                }
            }
            
            // Special initialization for RunScriptCommand
            Command scriptCommand = getCommand("script");
            if (scriptCommand != null) {
                try {
                    scriptCommand.getClass().getMethod("setCommandExecutor", CommandExecutor.class)
                        .invoke(scriptCommand, this);
                    System.out.println("Script successfully initialized");
                } catch (Exception e) {
                    System.err.println("Script: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error discovering commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get a command by name or alias.
     * 
     * @param name Command name or alias
     * @return Command instance or null if not found
     */
    public Command getCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }
    
    /**
     * Get all registered commands.
     * 
     * @return Map of command names to Command instances
     */
    public Map<String, Command> getCommands() {
        return new HashMap<>(commandMap);
    }
    
    /**
     * Execute a parsed command.
     * 
     * @param parsedCommand The parsed command to execute
     * @param consoleUI Reference to the console UI for commands that need UI access
     * @return Result of the command execution
     */
    public CommandResult execute(ParsedCommand parsedCommand, ConsoleUI consoleUI) {
        String commandName = parsedCommand.getCommandName();
        Command command = getCommand(commandName);
        
        if (command == null) {
            return new CommandResult(true, "Unknown command: " + commandName);
        }
        
        // Special handling for ClearCommand
        if (command.getName().equals("clear") && consoleUI != null) {
            consoleUI.clearOutput();
        }
        
        return command.execute(parsedCommand.getArgs());
    }
}
