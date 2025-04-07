package command.impl;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;

/**
 * Command to clear the console output.
 */
@CommandAlias({"cls","clear"})
public class Clear implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        // The actual clearing is done in CommandExecutor, since it requires UI access
        // This just returns a blank result
        return new CommandResult(false, "");
    }
    
    @Override
    public String getName() {
        return "clear";
    }
    
    @Override
    public String getDescription() {
        return "Clears the console output";
    }
    
    @Override
    public String getUsage() {
        return "clear";
    }
}