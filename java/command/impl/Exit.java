package command.impl;

import java.util.List;
import command.Command;
import command.CommandAlias;
import command.CommandResult;
import javafx.application.Platform;

/**
 * Command to exit the application.
 */
@CommandAlias({"exit"})
public class Exit implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        // Schedule application exit on the JavaFX Application Thread
        Platform.runLater(() -> Platform.exit());
        return CommandResult.success("Exiting application...");
    }
    
    @Override
    public String getName() {
        return "exit";
    }
    
    @Override
    public String getDescription() {
        return "Exits the application.";
    }
    
    @Override
    public String getUsage() {
        return "exit";
    }
}
