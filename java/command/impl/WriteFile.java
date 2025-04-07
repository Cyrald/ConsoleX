package command.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;
import commandUtils.VariableManager;

/**
 * Command to write content to a file.
 */
@CommandAlias({"write"})
public class WriteFile implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.size() < 2) {
            return CommandResult.error("Usage: " + getUsage());
        }
        
        String pathStr = args.get(0);
        
        // Process variables in the path
        pathStr = VariableManager.processVariables(pathStr);
        
        // Resolve the path
        Path filePath = FileManager.resolvePath(pathStr);
        
        // Check for append flag
        boolean append = args.contains("-a");
        int contentStartIndex = append ? 2 : 1;
        
        // Get content to write
        List<String> content = new ArrayList<>();
        if (args.size() > contentStartIndex) {
            StringBuilder contentStr = new StringBuilder();
            for (int i = contentStartIndex; i < args.size(); i++) {
                if (args.get(i).equals("-a")) {
                    continue; // Skip append flag
                }
                if (contentStr.length() > 0) {
                    contentStr.append(" ");
                }
                contentStr.append(args.get(i));
            }
            
            // Process variables in content
            String processedContent = VariableManager.processVariables(contentStr.toString());
            
            // Split by newlines
            for (String line : processedContent.split("\\\\n")) {
                content.add(line);
            }
        }
        
        try {
            // Create parent directories if needed
            Path parent = filePath.getParent();
            if (parent != null) {
                FileManager.createDirectory(parent);
            }
            
            // Write content to file
            FileManager.writeFile(filePath, content, append);
            
            String message = append ? 
                "Content appended to file: " : 
                "Content written to file: ";
            return CommandResult.success(message + filePath);
        } catch (IOException e) {
            return CommandResult.error("Error writing to file: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "write";
    }
    
    @Override
    public String getDescription() {
        return "Writes content to a file.";
    }
    
    @Override
    public String getUsage() {
        return "write <file> [-a] <content>";
    }
}
