package command.impl;

import java.util.List;
import java.util.Map;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.Cache;

/**
 * Command for working with the cache.
 * Supports operations: put, get, remove, clear, list.
 */
@CommandAlias({"cache"})
public class CacheCommand implements Command {
    
    @Override
    public String getName() {
        return "cache";
    }
    
    @Override
    public String getDescription() {
        return "Cache management. Available operations: set, get, remove, clear, list";
    }
    
    @Override
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append("cache set <key> <value> - Save a value to the cache\n");
        usage.append("cache get <key> - Get a value from the cache\n");
        usage.append("cache remove <key> - Remove a value from the cache\n");
        usage.append("cache clear - Clear the cache\n");
        usage.append("cache list - Show all entries in the cache");
        return usage.toString();
    }
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new CommandResult(true, "Operation must be specified. Available operations: put, get, remove, clear, list");
        }
        
        String operation = args.get(0).toLowerCase();
        
        switch (operation) {
            case "set":
                return handlePut(args);
            case "get":
                return handleGet(args);
            case "remove":
                return handleRemove(args);
            case "clear":
                return handleClear();
            case "list":
                return handleList();
            default:
                return new CommandResult(true, "Unknown operation: " + operation + 
                        "\nAvailable operations: set, get, remove, clear, list");
        }
    }
    
    private CommandResult handlePut(List<String> args) {
        if (args.size() < 3) {
            return new CommandResult(true, "Not enough arguments. Usage: cache set <key> <value>");
        }
        
        String key = args.get(1);
        // Join all remaining arguments into a single value
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 2; i < args.size(); i++) {
            if (i > 2) {
                valueBuilder.append(" ");
            }
            valueBuilder.append(args.get(i));
        }
        String value = valueBuilder.toString();
        
        Cache.put(key, value);
        return new CommandResult(false, "Value saved to cache: " + key + " = " + value);
    }
    
    private CommandResult handleGet(List<String> args) {
        if (args.size() < 2) {
            return new CommandResult(true, "Not enough arguments. Usage: cache get <key>");
        }
        
        String key = args.get(1);
        Object value = Cache.get(key);
        
        if (value == null) {
            return new CommandResult(true, "Key not found in cache: " + key);
        }
        
        return new CommandResult(false, key + " = " + value.toString());
    }
    
    private CommandResult handleRemove(List<String> args) {
        if (args.size() < 2) {
            return new CommandResult(true, "Not enough arguments. Usage: cache remove <key>");
        }
        
        String key = args.get(1);
        boolean removed = Cache.remove(key);
        
        if (removed) {
            return new CommandResult(false, "Key removed from cache: " + key);
        } else {
            return new CommandResult(true, "Key not found in cache: " + key);
        }
    }
    
    private CommandResult handleClear() {
        int size = Cache.size();
        Cache.clear();
        return new CommandResult(false, "Cache cleared. Entries removed: " + size);
    }
    
    private CommandResult handleList() {
        Map<String, Object> allEntries = Cache.getAll();
        
        if (allEntries.isEmpty()) {
            return new CommandResult(false, "Cache is empty");
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Entries in cache (" + allEntries.size() + "):\n");
        
        for (Map.Entry<String, Object> entry : allEntries.entrySet()) {
            result.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        
        return new CommandResult(false, result.toString().trim());
    }
}