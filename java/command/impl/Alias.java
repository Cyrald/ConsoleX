package command.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import command.Command;
import command.CommandAlias;
import command.CommandResult;
import commandUtils.Cache;

/**
 * Command for creating and managing command aliases. Allows creating custom
 * aliases for existing commands.
 */
@CommandAlias({ "alias" })
public class Alias implements Command {

	private static final String ALIAS_CACHE_PREFIX = "alias_";

	@Override
	public String getName() {
		return "alias";
	}

	@Override
	public String getDescription() {
		return "Creating and managing command aliases";
	}

	@Override
	public String getUsage() {
		StringBuilder usage = new StringBuilder();
		usage.append("alias <alias> <command> - Create an alias for a command\n");
		usage.append("alias list - Show all created aliases\n");
		usage.append("alias remove <alias> - Remove an alias\n");
		usage.append("alias clear - Remove all aliases");
		return usage.toString();
	}

	@Override
	public CommandResult execute(List<String> args) {
		if (args.isEmpty()) {
			return new CommandResult(true,
					"Operation or alias must be specified. Use 'alias list' to view all aliases.");
		}

		String first = args.get(0).toLowerCase();

		// Process list, remove and clear commands
		switch (first) {
		case "list":
			return listAliases();
		case "remove":
			if (args.size() < 2) {
				return new CommandResult(true, "Alias name must be specified for removal");
			}
			return removeAlias(args.get(1));
		case "clear":
			return clearAliases();
		}

		// Create a new alias
		if (args.size() < 2) {
			return new CommandResult(true, "Not enough arguments. Usage: alias <alias> <command>");
		}

		String aliasName = args.get(0);

		// Join remaining arguments to form the command
		StringBuilder commandBuilder = new StringBuilder();
		for (int i = 1; i < args.size(); i++) {
			if (i > 1) {
				commandBuilder.append(" ");
			}
			commandBuilder.append(args.get(i));
		}
		String command = commandBuilder.toString();

		return createAlias(aliasName, command);
	}

	/**
	 * Create a new alias for a command.
	 * 
	 * @param aliasName Alias name
	 * @param command   Command to be executed
	 * @return Operation result
	 */
	private CommandResult createAlias(String aliasName, String command) {
		// Check if the alias conflicts with system commands
		if (isSystemCommand(aliasName)) {
			return new CommandResult(true, "Cannot create an alias with a system command name: " + aliasName);
		}

		String cacheKey = ALIAS_CACHE_PREFIX + aliasName;
		Cache.put(cacheKey, command);

		return new CommandResult(false, "Alias created: " + aliasName + " -> " + command);
	}

	/**
	 * Display a list of all created aliases.
	 * 
	 * @return Operation result
	 */
	private CommandResult listAliases() {
		Map<String, Object> allAliases = getAllAliases();

		if (allAliases.isEmpty()) {
			return new CommandResult(false, "No aliases created");
		}

		StringBuilder result = new StringBuilder();
		result.append("Created aliases (" + allAliases.size() + "):\n");

		for (Map.Entry<String, Object> entry : allAliases.entrySet()) {
			result.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
		}

		return new CommandResult(false, result.toString().trim());
	}

	/**
	 * Remove an alias.
	 * 
	 * @param aliasName Alias name to remove
	 * @return Operation result
	 */
	private CommandResult removeAlias(String aliasName) {
		String cacheKey = ALIAS_CACHE_PREFIX + aliasName;

		if (Cache.containsKey(cacheKey)) {
			Cache.remove(cacheKey);
			return new CommandResult(false, "Alias removed: " + aliasName);
		} else {
			return new CommandResult(true, "Alias not found: " + aliasName);
		}
	}

	/**
	 * Remove all aliases.
	 * 
	 * @return Operation result
	 */
	private CommandResult clearAliases() {
		Map<String, Object> allAliases = getAllAliases();
		int count = allAliases.size();

		if (count == 0) {
			return new CommandResult(false, "No created aliases");
		}

		for (String aliasName : allAliases.keySet()) {
			Cache.remove(ALIAS_CACHE_PREFIX + aliasName);
		}

		return new CommandResult(false, "Removed all aliases (" + count + ")");
	}

	/**
	 * Get all created aliases.
	 * 
	 * @return Map of aliases and their corresponding commands
	 */
	public static Map<String, Object> getAllAliases() {
		Map<String, Object> all = Cache.getAll();
		Map<String, Object> aliases = new HashMap<>();

		for (Map.Entry<String, Object> entry : all.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(ALIAS_CACHE_PREFIX)) {
				String aliasName = key.substring(ALIAS_CACHE_PREFIX.length());
				aliases.put(aliasName, entry.getValue());
			}
		}

		return aliases;
	}

	/**
	 * Checks if the string is a system command name.
	 * 
	 * @param name Name to check
	 * @return true if it's a system command name
	 */
	private boolean isSystemCommand(String name) {
		// List of commands for which aliases cannot be created
		String[] systemCommands = { "alias", "cache", "calc", "cd", "cls", "clear", "env", "exit", "help", "dir", "ls",
				"mkdir", "touch", "open", "print", "echo", "read", "rm", "remove", "delete", "del", "script", "var",
				"write" };

		String nameLower = name.toLowerCase();
		for (String cmd : systemCommands) {
			if (cmd.equals(nameLower)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the string is an alias name.
	 * 
	 * @param name Name to check
	 * @return true if it's an existing alias name
	 */
	public static boolean isAlias(String name) {
		return Cache.containsKey(ALIAS_CACHE_PREFIX + name);
	}

	/**
	 * Gets the command corresponding to an alias.
	 * 
	 * @param aliasName Alias name
	 * @return Command corresponding to the alias, or null if alias not found
	 */
	public static String getAliasCommand(String aliasName) {
		Object cmd = Cache.get(ALIAS_CACHE_PREFIX + aliasName);
		return cmd != null ? cmd.toString() : null;
	}
}