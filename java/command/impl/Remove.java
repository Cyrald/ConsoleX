package command.impl;

import java.nio.file.Path;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to remove files or directories.
 */
@CommandAlias({"rm", "remove", "delete", "del"})
public class Remove implements Command {
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new CommandResult(true, "Usage: " + getUsage());
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        boolean allSuccessful = true;
        
        for (String pathStr : args) {
            // Process variables in the path
            pathStr = VariableManager.processVariables(pathStr);
            
            // Resolve the path
            Path path = FileManager.resolvePath(pathStr);
            
            if (!FileManager.fileExists(path)) {
                resultBuilder.append("File or directory does not exist: ").append(path).append("\n");
                allSuccessful = false;
                continue;
            }
            
            boolean isSuccess = FileManager.delete(path);
            
            if (!isSuccess) {
                resultBuilder.append("Failed to delete: ").append(path).append("\n");
                allSuccessful = false;
            }
        }
        
        if (allSuccessful) {
            return new CommandResult(false, "Deletion completed successfully.");
        } else {
            return new CommandResult(true, resultBuilder.toString().trim());
        }
    }
    
    @Override
    public String getName() {
        return "rm";
    }
    
    @Override
    public String getDescription() {
        return "Removes files or directories.";
    }
    
    @Override
    public String getUsage() {
        return "rm <path1> [<path2> ...]";
    }
}