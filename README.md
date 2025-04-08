# 📋 Available Commands

### 📁 File Operations
- `cd <path>` - Change directory
- `ls [path]` - List files and directories
- `mkdir <path>` - Create directory
- `rm <path> [path2] [...]` - Remove files/directories
- `readfile <path>` - Display file contents
- `writefile <path> <content>` - Write to file
- `open <file_path>` - Open file in default app

### 🏷️ Variable Management
- `var set <n> <value>` - Set variable value
- `var get <n>` - Get variable value
- `var list` - List all variables
- `var clear` - Clear all variables

### 🌐 Environment Variables
- `env list` - Show all environment variables
- `env get <n>` - Get environment variable
- `env set <n> <value>` - Set environment variable

### 💾 Cache Management
- `cache put <key> <value>` - Save to cache
- `cache get <key>` - Get from cache
- `cache remove <key>` - Remove from cache
- `cache clear` - Clear cache
- `cache list` - Show all cache entries

### 🧮 Calculation and Utilities
- `calc <expression>` - Calculate expression
- `clear` - Clear console output
- `exit` - Exit application
- `help [command]` - Show help information
- `print <text>` - Print text to console

### 📋 Script and Alias Management
- `script <script_file>` - Run script file
- `alias <alias> <command>` - Create command alias
- `alias list` - Show all aliases
- `alias remove <alias>` - Remove alias
- `alias clear` - Remove all aliases

## 🔌 Creating Plugins

Create plugins to extend the application with custom commands:

```java
public class MyPlugin extends AbstractPlugin {
    
    public MyPlugin() {
        // Add commands in constructor
        addCommand(new MyCommand());
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "Your Name";
    }
    
    @Override
    public String getDescription() {
        return "Does something cool";
    }
}

// Create a command to be added to the plugin
public class MyCommand implements Command {
    
    @Override
    public String getName() {
        return "mycommand";
    }
    
    @Override
    public String getDescription() {
        return "Does something cool";
    }
    
    @Override
    public String getUsage() {
        return "mycommand [arg1] [arg2]";
    }
    
    @Override
    public CommandResult execute(List<String> args) {
        // Command implementation
        return new CommandResult(false, "Command executed!");
    }
}
```


## 🚀 Usage Examples

```
# Working with environment variables
print "User name: $(env get username)"

# Using variables and calculation with multiple commands
var set m 12; calc -12 + $(var get m)

# Using variables for paths
var set project_dir "C:/MyProject"
cd $(var get project_dir)
ls

# Create aliases for command sequences
alias showinfo print "User: $(env get username), OS: $(env get os)"
showinfo

# Multiple operations in one line
var set a 5; var set b 10; calc $(var get a) * $(var get b)

# Unlimited nesting
echo $(echo $(echo $(echo $(echo $(echo $(echo "Hello World!"))))))

```

## MIT License
