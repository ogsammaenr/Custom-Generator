package xanth.ogsammaenr.customGenerator.storage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.model.CustomGeneratorCategory;

import java.io.File;
import java.util.*;

/**
 * Responsible for loading custom generator categories from the config file
 * and registering them in the {@link CustomCategoryManager}.
 */
public class CustomCategoryLoader {
    private final CustomGenerator plugin;
    private final CustomCategoryManager manager;

    /**
     * Constructs a loader for custom generator categories.
     *
     * @param plugin the main plugin instance
     */
    public CustomCategoryLoader(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCustomCategoryManager();
    }

    /**
     * Loads all custom generator categories from the 'custom-generator-categories.yml' file
     * and registers them to the {@link CustomCategoryManager}.
     * If any section is invalid or missing required fields, it will be skipped with a warning.
     */
    public void loadCustomCategory() {
        File file = new File(plugin.getDataFolder(), "custom-generator-categories.yml");
        if (!file.exists()) {
            plugin.saveResource("custom-generator-categories.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("custom-categories");
        if (section == null) {
            plugin.getLogger().warning("Custom generator categories file is missing!");
            return;
        }
        manager.clear();

        for (String key : section.getKeys(false)) {
            ConfigurationSection catsec = section.getConfigurationSection(key);
            if (catsec == null) continue;

            try {
                CustomGeneratorCategory category = parseCategory(key, catsec);
                manager.addCategory(category);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load custom category '" + key + "': " + e.getMessage());
            }
        }

    }

    /**
     * Parses a single custom generator category from the configuration section.
     *
     * @param id  the ID of the category from the config path (e.g., "dirt_generator")
     * @param sec the configuration section containing the category definition
     * @return a {@link CustomGeneratorCategory} object created from config values
     * @throws IllegalArgumentException if required fields (e.g., fluid) are missing or invalid
     */
    private CustomGeneratorCategory parseCategory(String id, ConfigurationSection sec) {
        String categoryId = sec.getString("category");
        String displayNameRaw = sec.getString("display-name", categoryId);
        String displayName = ChatColor.translateAlternateColorCodes('&', displayNameRaw);
        Material fluid = Material.matchMaterial(sec.getString("fluid"));
        if (fluid == null) {
            throw new IllegalArgumentException("Invalid or missing fluid type");
        }
        Material to = sec.contains("to") ? Material.matchMaterial(sec.getString("to")) : null;

        // blockConditions
        EnumMap<CustomGeneratorCategory.Direction, List<Material>> blockConditions = new EnumMap<>(CustomGeneratorCategory.Direction.class);
        ConfigurationSection condSec = sec.getConfigurationSection("conditions");
        if (condSec != null) {
            for (String dirKey : condSec.getKeys(false)) {
                CustomGeneratorCategory.Direction dir = CustomGeneratorCategory.Direction.valueOf(dirKey.toUpperCase());
                List<Material> materials = condSec.getStringList(dirKey).stream()
                        .map(Material::matchMaterial)
                        .filter(Objects::nonNull)
                        .toList();
                blockConditions.put(dir, materials);
            }
        }

        // y-level
        Integer minY = null;
        Integer maxY = null;
        if (sec.isConfigurationSection("y-level")) {
            ConfigurationSection ySec = sec.getConfigurationSection("y-level");
            if (ySec.contains("min")) minY = ySec.getInt("min");
            if (ySec.contains("max")) maxY = ySec.getInt("max");
        }

        // biomes
        Set<Biome> biomes = new HashSet<>();
        if (sec.isList("biomes")) {
            for (String b : sec.getStringList("biomes")) {
                Biome biome = Registry.BIOME.get(NamespacedKey.fromString(b));
                if (biome != null) {
                    biomes.add(biome);
                }
            }
        }

        return new CustomGeneratorCategory(
                categoryId,
                displayName,
                fluid,
                to,
                blockConditions,
                minY,
                maxY,
                biomes
        );
    }
}
