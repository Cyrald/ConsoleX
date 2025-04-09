package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import command.Command;
import command.CommandExecutor;
import command.CommandParser;
import command.CommandResult;
import command.ParsedCommand;
import command.impl.Alias;
import commandUtils.FileManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Main UI component for the console application. This class handles user input
 * and displays command results.
 */
public class ConsoleUI extends BorderPane {
	private final WebView webView; // TextFlow for color formatting
	private final WebEngine webEngine;
	private final TextField inputField;
	private final CommandExecutor commandExecutor;
	private final CommandParser commandParser;
	private final List<String> commandHistory;
	private int historyIndex = -1;
	private String lastTabCompletion = null;
	private List<String> tabCompletionOptions = new ArrayList<>();
	private int tabCompletionIndex = 0;

	public ConsoleUI() {
		// Component initialization
		this.getStyleClass().add("console-ui");
		commandExecutor = new CommandExecutor();
		commandParser = new CommandParser();
		commandParser.setCommandExecutor(commandExecutor);
		commandHistory = new ArrayList<>();

		// Output area setup - using TextFlow for color formatting
		webView = new WebView();
		webView.getStyleClass().add("console");
		webEngine = webView.getEngine();
		String cssPath = getClass().getResource("/ui/console.css").toExternalForm();
		webEngine.setUserStyleSheetLocation(cssPath);
		webEngine.load(getClass().getResource("/ui/console.html").toExternalForm());

		// Input field setup
		inputField = new TextField();
		inputField.getStyleClass().add("console-input");
		inputField.setPromptText("Enter command...");

		// Setup context menu for input field
		setupInputFieldContextMenu();

		// Component layout
		setCenter(webView);
		setBottom(inputField);
		setPadding(new Insets(10));

		// Welcome message initialization
		appendToOutput("Welcome to ConsoleX, " + System.getProperty("user.name")
				+ "!\nType 'help' to view available commands.\n", Color.LIGHTGREEN);
		appendToOutput(FileManager.getCurrentDirectory().toString() + " > \n", Color.WHITE);

		// Command input handling
		inputField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				String input = inputField.getText().trim();

				if (!input.isEmpty()) {
					// Add command to history
					if (!commandHistory.contains(input))
						commandHistory.add(input);
					historyIndex = commandHistory.size();

					// Execute command(s)
					try {
						// Split input by semicolons to handle multiple commands
						String[] commands = input.split(";");

						boolean isFirst = true;

						for (String command : commands) {
							String trimmedCommand = command.trim();
							if (trimmedCommand.isEmpty()) {
								continue; // Skip empty commands (e.g., if input ends with semicolon)
							}

							// Parse and execute the command
							ParsedCommand parsedCommand = commandParser.parse(trimmedCommand);
							if (parsedCommand != null) {
								CommandResult result = commandExecutor.execute(parsedCommand, this);
								if (isFirst) {
									if (!(parsedCommand.getCommandName().equals("clear") && parsedCommand.getCommandName().equals("cls")))
										appendToOutput(
												FileManager.getCurrentDirectory().toString() + " > " + input + "\n",
												Color.WHITE);
									isFirst = false;
								}
								if (result.hasOutput()) {

									if (result.isError()) {
										appendToOutput(result.getOutput() + "\n", Color.RED);
									} else {
										appendToOutput(result.getOutput() + "\n", Color.LIGHTBLUE);
									}
								}
							}
						}
					} catch (Exception e) {
						appendToOutput("Error: " + e.getMessage() + "\n", Color.RED);
					}

					// Clear input field
					inputField.clear();
					lastTabCompletion = null;
				}
			} else if (event.getCode() == KeyCode.UP) {
				navigateHistory(-1);
				event.consume();
				lastTabCompletion = null;
			} else if (event.getCode() == KeyCode.DOWN) {
				navigateHistory(1);
				event.consume();
				lastTabCompletion = null;
			} else if (event.getCode() == KeyCode.TAB) {
				event.consume();
				handleTabCompletion();
			} else {
				lastTabCompletion = null;
			}
		});

	}

	/**
	 * Sets up context menu for the input field
	 */
	private void setupInputFieldContextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		// Create menu items
		MenuItem copyItem = new MenuItem("Copy");
		MenuItem pasteItem = new MenuItem("Paste");
		MenuItem selectAllItem = new MenuItem("Select All");

		// Setup menu item actions
		copyItem.setOnAction(e -> {
			inputField.copy();
		});

		pasteItem.setOnAction(e -> {
			inputField.paste();
		});

		selectAllItem.setOnAction(e -> {
			inputField.selectAll();
		});

		// Add items to menu without separators and without Cut
		contextMenu.getItems().addAll(copyItem, pasteItem, selectAllItem);

		// Set menu for input field
		inputField.setContextMenu(contextMenu);
	}

	/**
	 * Handles tab key auto-completion
	 */
	private void handleTabCompletion() {
		String currentInput = inputField.getText().trim();

		// If input is empty, show all available commands
		if (currentInput.isEmpty()) {
			Set<String> commands = getAvailableCommands();
			appendToOutput("Available commands:\n" + String.join(", ", commands) + "\n", Color.GRAY);
			return;
		}

		// Check if this is a continuation of a previous Tab
		if (lastTabCompletion != null && currentInput.equals(lastTabCompletion) && !tabCompletionOptions.isEmpty()) {
			// Move to the next option in the list
			tabCompletionIndex = (tabCompletionIndex + 1) % tabCompletionOptions.size();
			String nextCompletion = tabCompletionOptions.get(tabCompletionIndex);
			inputField.setText(nextCompletion);
			inputField.positionCaret(nextCompletion.length());
			lastTabCompletion = nextCompletion;
			return;
		}

		// New auto-completion
		Set<String> commands = getAvailableCommands();
		tabCompletionOptions.clear();

		// Filter commands that start with the current input
		tabCompletionOptions = commands.stream().filter(cmd -> cmd.startsWith(currentInput))
				.collect(Collectors.toList());

		if (tabCompletionOptions.isEmpty()) {
			// No matches
			return;
		} else if (tabCompletionOptions.size() == 1) {
			// Exact match
			String completion = tabCompletionOptions.get(0);
			inputField.setText(completion);
			inputField.positionCaret(completion.length());
			lastTabCompletion = completion;
			tabCompletionIndex = 0;
		} else {
			// Multiple matches, show all options
			appendToOutput("Options: " + String.join(", ", tabCompletionOptions) + "\n", Color.GRAY);

			// Find common prefix
			String commonPrefix = findCommonPrefix(tabCompletionOptions);
			if (commonPrefix.length() > currentInput.length()) {
				inputField.setText(commonPrefix);
				inputField.positionCaret(commonPrefix.length());
				lastTabCompletion = commonPrefix;
				tabCompletionIndex = 0;
			}
		}
	}

	/**
	 * Finds the common prefix for a list of strings
	 */
	private String findCommonPrefix(List<String> strings) {
		if (strings.isEmpty()) {
			return "";
		}

		String first = strings.get(0);
		int prefixLength = first.length();

		for (int i = 1; i < strings.size(); i++) {
			String current = strings.get(i);
			int j = 0;
			while (j < prefixLength && j < current.length() && first.charAt(j) == current.charAt(j)) {
				j++;
			}
			prefixLength = j;
		}

		return first.substring(0, prefixLength);
	}

	/**
	 * Gets a list of available commands, including aliases
	 */
	private Set<String> getAvailableCommands() {
		Set<String> commands = new TreeSet<>();

		// Add all system commands
		for (Command cmd : commandExecutor.getCommands().values()) {
			commands.add(cmd.getName());
		}

		// Add all user aliases
		try {
			// Get all aliases from cache
			Map<String, Object> allAliases = Alias.getAllAliases();

			// Add alias names to the command list
			for (String aliasName : allAliases.keySet()) {
				commands.add(aliasName);
			}
		} catch (Exception e) {
			// Ignore errors when getting aliases
			System.err.println("Error getting aliases: " + e.getMessage());
		}

		return commands;
	}

	/**
	 * Navigation through command history
	 */
	private void navigateHistory(int direction) {
		if (commandHistory.isEmpty()) {
			return;
		}

		historyIndex += direction;

		if (historyIndex < 0) {
			historyIndex = 0;
		} else if (historyIndex >= commandHistory.size()) {
			historyIndex = commandHistory.size();
			inputField.clear();
			return;
		}

		if (historyIndex < commandHistory.size()) {
			inputField.setText(commandHistory.get(historyIndex));
			inputField.positionCaret(inputField.getText().length());
		}
	}

	/**
	 * Adds text to the console output with the specified color
	 */

	@SuppressWarnings("exports")
	public void appendToOutput(String text, Color color) {
		if (webEngine == null)
			return;

		Platform.runLater(() -> {
			try {
				String colorName = getColorName(color);
				webEngine.executeScript("if (typeof appendToConsole === 'function') { appendToConsole('"
						+ escapeJavaScript(text) + "', '" + colorName + "'); }");
			} catch (Exception e) {
				System.err.println("Ошибка при добавлении текста: " + e.getMessage());
			}
		});
	}

	private String getColorName(Color color) {
		if (color == null)
			return "white";

		if (color.equals(Color.WHITE))
			return "white";
		if (color.equals(Color.LIGHTGREEN))
			return "lightgreen";
		if (color.equals(Color.LIGHTBLUE))
			return "lightblue";
		if (color.equals(Color.RED))
			return "red";
		if (color.equals(Color.GRAY))
			return "gray";

		// Default fallback
		return "white";
	}

	/**
	 * Escape special characters for JavaScript strings
	 */
	private String escapeJavaScript(String text) {
		if (text == null)
			return "";

		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Clears the console output
	 */
	public void clearOutput() {
		if (webEngine == null)
			return;

		Platform.runLater(() -> {
			try {
				webEngine.executeScript("if (typeof clearConsole === 'function') { clearConsole(); }");
			} catch (Exception e) {
				System.err.println("Ошибка при очистке консоли: " + e.getMessage());
			}
		});
	}

	/**
	 * Sets focus on the input field
	 */
	public void focusInput() {
		Platform.runLater(() -> inputField.requestFocus());
	}

	/**
	 * Java bridge class for JavaScript interop
	 */
	public class JavaBridge {
		/**
		 * Handle command submission from JavaScript
		 */
		/*
		 * public void handleCommand(String command) { executeCommand(command); }
		 */

		/**
		 * Handle tab completion from JavaScript
		 */
		public void handleTabCompletion(String input) {
			handleTabCompletion(input);
		}
	}
}
