package commandUtils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages file operations for the console application.
 */
public class FileManager {
    // Current working directory
    private static Path currentDirectory = Paths.get(System.getProperty("user.dir"));
    
    /**
     * Get the current working directory.
     * 
     * @return Path of the current directory
     */
    public static Path getCurrentDirectory() {
        return currentDirectory;
    }
    
    
    public static void move(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Open a file with the default application
     * @param path The file path to open
     * @return true if the file was opened successfully, false otherwise
     */
    public static boolean openFile(Path path) {
        if (!Files.exists(path)) {
            return false;
        }
        
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(path.toFile());
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Set the current working directory.
     * 
     * @param directory New current directory path
     * @return true if successful, false otherwise
     */
    public static boolean setCurrentDirectory(Path directory) {
        if (Files.isDirectory(directory)) {
            currentDirectory = directory;
            return true;
        }
        return false;
    }
    
    /**
     * Resolve a path string against the current directory.
     * Handles relative and absolute paths.
     * 
     * @param pathStr Path string to resolve
     * @return Resolved path
     */
    public static Path resolvePath(String pathStr) {
        Path path = Paths.get(pathStr);
        if (path.isAbsolute()) {
            return path;
        } else {
            return currentDirectory.resolve(path).normalize();
        }
    }
    
    /**
     * Read the contents of a file.
     * 
     * @param filePath Path to the file
     * @return List of lines from the file
     * @throws IOException If an I/O error occurs
     */
    public static List<String> readFile(Path filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
    
    /**
     * Write content to a file.
     * 
     * @param filePath Path to the file
     * @param content List of lines to write
     * @param append Whether to append to existing content
     * @throws IOException If an I/O error occurs
     */
    public static void writeFile(Path filePath, List<String> content, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), append))) {
            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    /**
     * List files and directories in a directory.
     * 
     * @param directory Directory to list
     * @return List of file/directory names
     * @throws IOException If an I/O error occurs
     */
    public static List<String> listDirectory(Path directory) throws IOException {
        List<String> result = new ArrayList<>();
        File[] files = directory.toFile().listFiles();
        
        if (files != null) {
            for (File file : files) {
                String entry = file.getName();
                if (file.isDirectory()) {
                    entry += "/";
                }
                result.add(entry);
            }
        }
        
        return result;
    }
    
    /**
     * Create a directory.
     * 
     * @param directoryPath Path to create
     * @return true if successful, false otherwise
     */
    public static boolean createDirectory(Path directoryPath) {
        try {
            Files.createDirectories(directoryPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Check if a file exists.
     * 
     * @param path Path to check
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(Path path) {
        return Files.exists(path);
    }
    
    /**
     * Delete a file or directory.
     * 
     * @param path Path to delete
     * @return true if successful, false otherwise
     */
    public static boolean delete(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(p -> {
                    try {
                        delete(p);
                    } catch (Exception e) {
                        // Ignore
                    }
                });
            }
            Files.delete(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Executes an application by name or path
     * @param appName Name or path of the application to execute
     * @param args Arguments to pass to the application
     * @return true if the application was launched successfully, false otherwise
     */
    public static boolean executeApplication(String appName, List<String> args) {
        try {
            // If the appName is an absolute path or exists in the current directory
            Path appPath = Paths.get(appName);
            if (appPath.isAbsolute() && Files.exists(appPath)) {
                return openFileOrApplication(appPath.toFile(), args);
            }
            
            // If appName is a relative path in the current directory
            Path currentDirApp = getCurrentDirectory().resolve(appName);
            if (Files.exists(currentDirApp)) {
                return openFileOrApplication(currentDirApp.toFile(), args);
            }
            
            // Search in PATH environment variable
            String pathEnv = System.getenv("PATH");
            if (pathEnv != null) {
                String[] pathDirs = pathEnv.split(File.pathSeparator);
                
                // For Windows, look for common executable extensions if no extension is specified
                List<String> extensions = new ArrayList<>();
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Check if app already has an extension
                    if (!appName.contains(".")) {
                        // Add common Windows executable extensions
                        extensions.add(".exe");
                        extensions.add(".cmd");
                        extensions.add(".bat");
                        extensions.add(".com");
                    } else {
                        // If it has an extension, just try it as is
                        extensions.add("");
                    }
                } else {
                    // On Unix-like systems, no extension is needed
                    extensions.add("");
                }
                
                for (String dir : pathDirs) {
                    for (String ext : extensions) {
                        Path execPath = Paths.get(dir, appName + ext);
                        if (Files.exists(execPath)) {
                            return openFileOrApplication(execPath.toFile(), args);
                        }
                    }
                }
            }
            
            // If not found, try direct command as last resort
            File file = new File(appName);
            return openFileOrApplication(file, args);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Opens a file or application using Desktop API with arguments if possible
     * @param file The file or application to open
     * @param args Arguments to pass to the application (if supported)
     * @return true if opened successfully, false otherwise
     */
    private static boolean openFileOrApplication(File file, List<String> args) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                
                // If we have arguments, we need special handling
                if (args != null && !args.isEmpty()) {
                    // For Windows, try to handle arguments differently
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        // For Windows, use cmd.exe to run with arguments
                        StringBuilder command = new StringBuilder();
                        command.append("\"").append(file.getAbsolutePath()).append("\"");
                        
                        for (String arg : args) {
                            command.append(" ").append(arg);
                        }
                        
                        // Use cmd /c to execute
                        String[] cmdArray = {"cmd.exe", "/c", "start", command.toString()};
                        Runtime.getRuntime().exec(cmdArray);
                        return true;
                    } else {
                        // For Unix-like systems, try direct execution
                        String[] cmdArray = new String[args.size() + 1];
                        cmdArray[0] = file.getAbsolutePath();
                        for (int i = 0; i < args.size(); i++) {
                            cmdArray[i + 1] = args.get(i);
                        }
                        Runtime.getRuntime().exec(cmdArray);
                        return true;
                    }
                } else {
                    // Simple case - no arguments, just open file with default app
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(file);
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Recursively delete a directory and all its contents.
     * 
     * @param directory The directory to delete
     * @throws IOException If an I/O error occurs
     */
    public static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            Files.delete(directory);
            return;
        }
        
        Files.walk(directory)
            .sorted((a, b) -> b.toString().length() - a.toString().length()) // Sort by descending path length to delete inner files first
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete: " + path, e);
                }
            });
    }
    
}    
    
