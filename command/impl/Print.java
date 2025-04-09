package command.impl;
import java.util.List;

import command.Command;
import command.CommandAlias;
import command.CommandResult;

/**
 * Command to echo input text back to the console.
 * Demonstrates basic command functionality.
 */
@CommandAlias({"print", "echo"})
public class Print implements Command {
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.success("");
        }
        
        return CommandResult.success(String.join(" ", args));
    }
    
    @Override
    public String getName() {
        return "print";
    }
    
    @Override
    public String getDescription() {
        return "Displays the given text";
    }
    
    @Override
    public String getUsage() {
        return "print [text]";
    }
}