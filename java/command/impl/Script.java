package command.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandExecutor;
import command.CommandParser;
import command.CommandResult;
import command.ParsedCommand;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to run a script file containing commands.
 */
@CommandAlias({"script"})
public class Script implements Command {
    private CommandExecutor commandExecutor;
    private CommandParser commandParser;
    
    /**
     * Default constructor used by reflection.
     * The CommandExecutor will be set after instantiation.
     */
    public Script() {
        this.commandParser = new CommandParser();
    }
    
    /**
     * Set the command executor reference.
     * This is called by the CommandExecutor after instantiating this command.
     * 
     * @param commandExecutor Reference to the command executor
     */
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
    
    @Override
    public CommandResult execute(List<String> args) {
        if (commandExecutor == null) {
            return new CommandResult(true, "Script has not been initialized correctly.");
        }
        
        if (args.isEmpty()) {
            return new CommandResult(true, "Usage: " + getUsage());
        }
        
        String pathStr = args.get(0);
        
        // Process variables in the path
        pathStr = VariableManager.processVariables(pathStr);
        
        // Resolve the path
        Path scriptPath = FileManager.resolvePath(pathStr);
        
        // Check if the file exists at the specified path
        if (!FileManager.fileExists(scriptPath)) {
            return new CommandResult(true, "Script file not found: " + scriptPath);
        }
        
        try {
            // Read the script file
            List<String> scriptLines = FileManager.readFile(scriptPath);
            List<String> results = new ArrayList<>();
            
            // Execute each line of the script
            for (String line : scriptLines) {
                // Skip empty lines and comments
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse the command
                ParsedCommand parsedCommand = commandParser.parse(line);
                if (parsedCommand != null) {
                    // Execute the command
                    CommandResult result = commandExecutor.execute(parsedCommand, null);
                    
                    // If there's output, add it to the results
                    if (result != null && !result.getOutput().isEmpty()) {
                        results.add(result.getOutput());
                    }
                }
            }
            
            // Return the script execution results
            if (results.isEmpty()) {
                return new CommandResult(false, "Script executed successfully with no output.");
            } else {
                return new CommandResult(false, String.join("\n", results));
            }
            
        } catch (IOException e) {
            return new CommandResult(true, "Error reading script file: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "script";
    }
    
    @Override
    public String getDescription() {
        return "Executes commands from a script file.";
    }
    
    @Override
    public String getUsage() {
        return "script <script_file>";
    }
}