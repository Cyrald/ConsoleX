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
 * Command to open a file with the system's default application.
 */
@CommandAlias({"open"})
public class Open implements Command {

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Open file in the default OS application";
    }

    @Override
    public String getUsage() {
        return "open <file_path>";
    }

    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new CommandResult(true, "Please specify the file path. Usage: " + getUsage());
        }

        // Get and process path from argument
        String filePath = args.get(0);
        filePath = VariableManager.processVariables(filePath);
        Path path = FileManager.resolvePath(filePath);
        
        // Check if file exists
        if (!Files.exists(path)) {
            return new CommandResult(true, "File does not exist: " + path);
        }
        
        // Check if it's a directory
        if (Files.isDirectory(path)) {
            return new CommandResult(true, "The specified path is a directory, not a file: " + path);
        }
        
        boolean success = FileManager.openFile(path);
        
        if (success) {
            return new CommandResult(false, "File opened: " + path);
        } else {
            return new CommandResult(true, 
                    "Failed to open the file. There may be no suitable application or the system function is not supported.");
        }
    }
}