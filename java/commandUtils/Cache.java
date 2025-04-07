package commandUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import command.Command;
import command.CommandResult;

/**
 * Implements a cache system for the console application.
 * The cache stores data in memory and can persist it to a file.
 */
public class Cache implements Command {
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final String CACHE_FILE = "console_cache.json";
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    private static boolean initialized = false;
    
    /**
     * Constructor for the command implementation.
     */
    public Cache() {
        initialize();
    }
    
    /**
     * Initialize the cache, loading saved values from the cache file.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            try {
                Map<String, Object> loadedCache = objectMapper.readValue(cacheFile, 
                    new TypeReference<Map<String, Object>>(){});
                if (loadedCache != null) {
                    cache.putAll(loadedCache);
                }
            } catch (IOException e) {
                System.err.println("Error loading cache: " + e.getMessage());
            }
        }
        
        initialized = true;
    }
    
    /**
     * Save the cache to a file.
     */
    public static void saveCache() {
        try {
            objectMapper.writeValue(new File(CACHE_FILE), cache);
        } catch (IOException e) {
            System.err.println("Error saving cache: " + e.getMessage());
        }
    }
    
    /**
     * Store a value in the cache.
     * 
     * @param key Cache key
     * @param value Value to store
     */
    public static void put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        initialize();
        cache.put(key, value);
        saveCache();
    }
    
    /**
     * Retrieve a value from the cache.
     * 
     * @param key Cache key
     * @return The cached value, or null if not found
     */
    public static Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        initialize();
        return cache.get(key);
    }
    
    /**
     * Remove a value from the cache.
     * 
     * @param key Cache key to remove
     * @return true if the key was removed, false if it did not exist
     */
    public static boolean remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        initialize();
        boolean removed = cache.remove(key) != null;
        if (removed) {
            saveCache();
        }
        return removed;
    }
    
    /**
     * Clear all entries from the cache.
     */
    public static void clear() {
        initialize();
        cache.clear();
        saveCache();
    }
    
    /**
     * Get all cache entries.
     * 
     * @return Map of all cache entries
     */
    public static Map<String, Object> getAll() {
        initialize();
        return new HashMap<>(cache);
    }
    
    /**
     * Check if a key exists in the cache.
     * 
     * @param key Cache key to check
     * @return true if the key exists, false otherwise
     */
    public static boolean containsKey(String key) {
        if (key == null) {
            return false;
        }
        initialize();
        return cache.containsKey(key);
    }
    
    /**
     * Get the number of entries in the cache.
     * 
     * @return Number of cache entries
     */
    public static int size() {
        initialize();
        return cache.size();
    }
    
    @Override
    public String getName() {
        return "cache";
    }
    
    @Override
    public String getDescription() {
        return "Manages application cache values";
    }
    
    @Override
    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        usage.append("cache get <key> - Get a value from cache\n");
        usage.append("cache set <key> <value> - Store a value in cache\n");
        usage.append("cache remove <key> - Remove a value from cache\n");
        usage.append("cache list - List all cache entries\n");
        usage.append("cache clear - Clear all cache entries");
        return usage.toString();
    }
    
    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return new CommandResult(true, "No operation specified. Use 'cache help' to see available operations.");
        }
        
        String operation = args.get(0).toLowerCase();
        
        switch (operation) {
            case "get":
                return handleGet(args);
            case "set":
                return handleSet(args);
            case "remove":
                return handleRemove(args);
            case "list":
                return handleList();
            case "clear":
                return handleClear();
            case "help":
                return new CommandResult(false, getUsage());
            default:
                return new CommandResult(true, "Unknown operation: " + operation + ". Use 'cache help' to see available operations.");
        }
    }
    
    private CommandResult handleGet(List<String> args) {
        if (args.size() < 2) {
            return new CommandResult(true, "No key specified. Usage: cache get <key>");
        }
        
        String key = args.get(1);
        Object value = get(key);
        
        if (value == null) {
            return new CommandResult(false, "Key not found: " + key);
        }
        
        return new CommandResult(false, key + " = " + value);
    }
    
    private CommandResult handleSet(List<String> args) {
        if (args.size() < 2) {
            return new CommandResult(true, "No key specified. Usage: cache set <key> <value>");
        }
        
        if (args.size() < 3) {
            return new CommandResult(true, "No value specified. Usage: cache set <key> <value>");
        }
        
        String key = args.get(1);
        
        // Combine remaining args as the value
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 2; i < args.size(); i++) {
            if (i > 2) {
                valueBuilder.append(" ");
            }
            valueBuilder.append(args.get(i));
        }
        
        String value = valueBuilder.toString();
        put(key, value);
        
        return new CommandResult(false, "Value stored: " + key + " = " + value);
    }
    
    private CommandResult handleRemove(List<String> args) {
        if (args.size() < 2) {
            return new CommandResult(true, "No key specified. Usage: cache remove <key>");
        }
        
        String key = args.get(1);
        boolean removed = remove(key);
        
        if (removed) {
            return new CommandResult(false, "Key removed: " + key);
        } else {
            return new CommandResult(false, "Key not found: " + key);
        }
    }
    
    private CommandResult handleList() {
        Map<String, Object> allCache = getAll();
        
        if (allCache.isEmpty()) {
            return new CommandResult(false, "Cache is empty");
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Cache entries (").append(allCache.size()).append("):\n");
        
        for (Map.Entry<String, Object> entry : allCache.entrySet()) {
            result.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        
        return new CommandResult(false, result.toString().trim());
    }
    
    private CommandResult handleClear() {
        int count = size();
        clear();
        return new CommandResult(false, "Cleared " + count + " cache entries");
    }
}
