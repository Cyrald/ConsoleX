package command.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to change the current directory.
 */
@CommandAlias({"cd"})
public class Cd implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            // With no args, show current directory
            Path currentDir = FileManager.getCurrentDirectory();
            return CommandResult.success(currentDir.toString());
        }
        
        String pathStr = args.get(0);
        
        // Process variables in the path
        pathStr = VariableManager.processVariables(pathStr);
        
        // Special case: cd ~ to go to user home
        if (pathStr.equals("~") || pathStr.startsWith("~/") || pathStr.startsWith("~\\")) {
            String userHome = System.getProperty("user.home");
            pathStr = pathStr.equals("~") ? userHome : userHome + pathStr.substring(1);
        }
        
        // Resolve the path
        Path newDir = FileManager.resolvePath(pathStr);
        
        // Check if it's a directory
        if (!Files.isDirectory(newDir)) {
            return CommandResult.error("Not a directory: " + newDir);
        }
        
        // Change directory
        if (!FileManager.setCurrentDirectory(newDir)) {
            return CommandResult.error("Failed to change directory.");
        }
        return CommandResult.success(null);
    }
    
    @Override
    public String getName() {
        return "cd";
    }
    
    @Override
    public String getDescription() {
        return "Changes the current directory.";
    }
    
    @Override
    public String getUsage() {
        return "cd [directory]";
    }
}
