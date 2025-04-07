package command.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to list files in a directory.
 */
@CommandAlias({"dir", "ls"})
public class Ls implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        Path directory;
        
        if (args.isEmpty()) {
            // List current directory if no args
            directory = FileManager.getCurrentDirectory();
        } else {
            String pathStr = args.get(0);
            
            // Process variables in the path
            pathStr = VariableManager.processVariables(pathStr);
            
            // Resolve the path
            directory = FileManager.resolvePath(pathStr);
        }
        
        try {
            List<String> entries = FileManager.listDirectory(directory);
            
            if (entries.isEmpty()) {
                return CommandResult.success("Directory is empty.");
            }
            
            // Sort entries alphabetically
            Collections.sort(entries);
            
            StringBuilder result = new StringBuilder();
            result.append("Directory: ").append(directory).append("\n\n");
            
            // Format entries in columns
            int maxLength = 0;
            for (String entry : entries) {
                maxLength = Math.max(maxLength, entry.length());
            }
            
            int columns = Math.max(1, 80 / (maxLength + 2));
            int rows = (int) Math.ceil((double) entries.size() / columns);
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    int index = i + j * rows;
                    if (index < entries.size()) {
                        String entry = entries.get(index);
                        result.append(String.format("%-" + (maxLength + 2) + "s", entry));
                    }
                }
                result.append("\n");
            }
            
            return CommandResult.success(result.toString());
        } catch (IOException e) {
            return CommandResult.error("Error listing directory: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "ls";
    }
    
    @Override
    public String getDescription() {
        return "Lists files and directories.";
    }
    
    @Override
    public String getUsage() {
        return "ls [directory]";
    }
}
