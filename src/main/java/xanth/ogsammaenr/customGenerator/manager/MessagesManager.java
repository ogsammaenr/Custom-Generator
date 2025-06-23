package xanth.ogsammaenr.customGenerator.manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {
    private final CustomGenerator plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<String, String> cache;

    public MessagesManager(CustomGenerator plugin) {
        this.plugin = plugin;

        cache = new HashMap<>();

        this.file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        load();
    }


    public void load() {
        this.config = YamlConfiguration.loadConfiguration(file);
        cache.clear();

        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                String raw = config.getString(key);
                if (raw != null) {
                    cache.put(key, ChatColor.translateAlternateColorCodes('&', raw));
                }
            }
        }

        for (GeneratorCategory category : GeneratorCategory.values()) {
            String key = "generator-category." + category.name().toLowerCase();
            String display = get(key);
            if (display != null) {
                category.setDisplayName(display);
            }
        }
    }

    public String get(String path) {
        return cache.getOrDefault(path, ChatColor.RED + "Missing message: " + path);
    }

    public String getFormatted(String path, Map<String, String> placeholders) {
        String message = get(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public void reload() {
        load();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
