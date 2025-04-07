package command.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to read the contents of a file.
 */
@CommandAlias({"read"})
public class ReadFile implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("Usage: " + getUsage());
        }
        
        String pathStr = args.get(0);
        
        // Process variables in the path
        pathStr = VariableManager.processVariables(pathStr);
        
        // Resolve the path
        Path filePath = FileManager.resolvePath(pathStr);
        
        if (!FileManager.fileExists(filePath)) {
            return CommandResult.error("File not found: " + filePath);
        }
        
        try {
            List<String> lines = FileManager.readFile(filePath);
            
            // Add line numbers if requested
            boolean showLineNumbers = args.contains("-n");
            if (showLineNumbers) {
                AtomicInteger lineNumber = new AtomicInteger(1);
                List<String> numberedLines = lines.stream()
                    .map(line -> String.format("%4d | %s", lineNumber.getAndIncrement(), line))
                    .collect(Collectors.toList());
                lines = numberedLines;
            }
            
            return CommandResult.success(String.join("\n", lines));
        } catch (IOException e) {
            return CommandResult.error("Error reading file: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "read";
    }
    
    @Override
    public String getDescription() {
        return "Reads and displays the contents of a file";
    }
    
    @Override
    public String getUsage() {
        return "read <file_path> [-n]";
    }
}