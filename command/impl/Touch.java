package command.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;

/**
 * Command to create an empty file.
 */
@CommandAlias({"touch"})
public class Touch implements Command {

    @Override
    public String getName() {
        return "touch";
    }

    @Override
    public String getDescription() {
        return "Creates an empty file";
    }

    @Override
    public String getUsage() {
        return "touch <file_path>";
    }

    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("File path is required");
        }

        String filePath = args.get(0);
        Path path = new File(filePath).toPath();
        
        try {
            // Create parent directories if they don't exist
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            // Create empty file if it doesn't exist
            if (!Files.exists(path)) {
                Files.createFile(path);
                return CommandResult.success("Created empty file: " + path);
            } else {
                // Update file timestamp if it already exists (like Unix touch)
                Files.setLastModifiedTime(path, java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));
                return CommandResult.success("Updated file timestamp: " + path);
            }
        } catch (IOException e) {
            return CommandResult.error("Failed to create file: " + e.getMessage());
        }
    }
}