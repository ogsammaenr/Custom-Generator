package xanth.ogsammaenr.customGenerator.storage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.model.IGeneratorCategory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeneratorTypeLoader {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;

    public GeneratorTypeLoader(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
    }

    ///     Dosyadan generator tiplerini yükler
    public void loadGeneratorTypes() {
        File file = new File(plugin.getDataFolder(), "generator-types.yml");
        if (!file.exists()) {
            plugin.saveResource("generator-types.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("generator-types");
        if (section == null) {
            plugin.getLogger().warning("generator-types.yml not found");
            return;
        }

        manager.getAllRegisteredTypes().clear();

        int priority = 0;
        for (String id : section.getKeys(false)) {
            ConfigurationSection genSec = section.getConfigurationSection(id);
            if (genSec == null) continue;

            String displayName = genSec.getString("display-name", id);
            String coloredDisplayName = ChatColor.translateAlternateColorCodes('&', displayName);
            String materialName = genSec.getString("material", "STONE");
            Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material == null) {
                plugin.getLogger().warning("Bilinmeyen material: " + materialName + " (ID: " + id + ")");
                continue;
            }

            List<String> lore = genSec.getStringList("lore");
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            String categoryName = genSec.getString("generator-type", "COBBLESTONE").toUpperCase();

            IGeneratorCategory category = plugin.getCustomCategoryManager().getCategoryById(categoryName);
            if (category == null) {
                try {
                    category = GeneratorCategory.valueOf(categoryName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz generator-type: " + categoryName + " (ID: " + id + ")");
                    continue;
                }
            }

            double price = genSec.getDouble("price", 0.0);
            int requiredLevel = genSec.getInt("required-island-level", 0);

            // Blok oranlarını oku
            Map<Material, Double> blockChances = new LinkedHashMap<>();
            ConfigurationSection blocksSec = genSec.getConfigurationSection("blocks");
            if (blocksSec == null) {
                plugin.getLogger().warning("Blok oranları eksik: " + id);
                continue;
            }

            for (String matName : blocksSec.getKeys(false)) {
                Material blockType = Material.matchMaterial(matName.toUpperCase());
                if (blockType == null) {
                    plugin.getLogger().warning("Bilinmeyen blok türü: " + matName + " (ID: " + id + ")");
                    continue;
                }
                double chance = blocksSec.getDouble(matName);
                blockChances.put(blockType, chance);
            }

            GeneratorType type = new GeneratorType(
                    id,
                    coloredDisplayName,
                    material,
                    coloredLore,
                    category,
                    price,
                    requiredLevel,
                    blockChances
            );
            type.setPriority(priority++);
            manager.registerGeneratorType(type);

            plugin.getLogger().info(id + " yüklendi");
            plugin.getLogger().info(type.toString());
            plugin.getLogger().info("==============================");
        }

        plugin.getLogger().info(priority + " jeneratör tipi yüklendi.");
        plugin.getLogger().info("==============================");
    }
}
