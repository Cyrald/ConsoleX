package command.impl;

import java.nio.file.Path;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to create a directory.
 */
@CommandAlias({"mkdir", "touch"})
public class Mkdir implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("Usage: " + getUsage());
        }
        
        String pathStr = args.get(0);
        
        // Process variables in the path
        pathStr = VariableManager.processVariables(pathStr);
        
        // Resolve the path
        Path dirPath = FileManager.resolvePath(pathStr);
        
        if (FileManager.fileExists(dirPath)) {
            return CommandResult.error("Path already exists: " + dirPath);
        }
        
        FileManager.createDirectory(dirPath);
		return CommandResult.success("Directory created: " + dirPath);
    }
    
    @Override
    public String getName() {
        return "mkdir";
    }
    
    @Override
    public String getDescription() {
        return "Creates a directory";
    }
    
    @Override
    public String getUsage() {
        return "mkdir <directory_path>";
    }
}
