package command.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.FileManager;

/**
 * Command to open a file with the system's default application or run an application from PATH.
 */
@CommandAlias({"open"})
public class Open implements Command {

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Open file in the default OS application or run an application from system PATH";
    }

    @Override
    public String getUsage() {
        return "open <file_path_or_app_name> [args...]";
    }

    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new CommandResult(true, "Please specify the file path or application name. Usage: " + getUsage());
        }

        // Get and process path/application name from first argument
        String pathOrApp = args.get(0);
        
        // Extract any additional arguments (if provided)
        List<String> appArgs = new ArrayList<>();
        if (args.size() > 1) {
            appArgs = args.subList(1, args.size());
        }
        
        // First try to resolve as a file path
        Path path = FileManager.resolvePath(pathOrApp);
        
        // If path exists as a file, open it with default application
        if (Files.exists(path) && !Files.isDirectory(path)) {
            boolean success = FileManager.openFile(path);
            
            if (success) {
                return new CommandResult(false, "File opened: " + path);
            } else {
                return new CommandResult(true, 
                        "Failed to open the file. There may be no suitable application or the system function is not supported.");
            }
        } 
        // If path is a directory, show error
        else if (Files.exists(path) && Files.isDirectory(path)) {
            return new CommandResult(true, "The specified path is a directory, not a file: " + path);
        } 
        // If file doesn't exist, try to run as an application from PATH
        else {
            boolean success = FileManager.executeApplication(pathOrApp, appArgs);
            
            if (success) {
                return new CommandResult(false, "Application launched: " + pathOrApp);
            } else {
                return new CommandResult(true, 
                        "Failed to open file or run application '" + pathOrApp + "'. File not found or application not available in PATH.");
            }
        }
    }
}