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
    
