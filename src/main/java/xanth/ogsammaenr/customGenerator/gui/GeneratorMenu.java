package xanth.ogsammaenr.customGenerator.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.ItemBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class GeneratorMenu {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;

    public GeneratorMenu(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
    }

    public void openMenu(Player player, GeneratorCategory selectedCategory) {
        List<GeneratorType> types = manager.getAllRegisteredTypes().values().stream()
                .filter(type -> selectedCategory == null || type.getGeneratorCategory() == selectedCategory)
                .collect(Collectors.toList());

        int size = 5 * 9;
        Inventory gui = Bukkit.createInventory(null, size, "Ada Jeneratörleri");
        //  ********** Filler Items **********
        ItemStack lineFiller = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7")
                .build();
        ItemStack backgroundFiller = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7")
                .build();

        for (int i = 0; i < size; i++) {
            gui.setItem(i, backgroundFiller);
        }
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, lineFiller);
        }
        for (int i = size - 9; i < size; i++) {
            gui.setItem(i, lineFiller);
        }

        //  ********** Generator Category Items *********
        int categoryIndex = 3;
        for (GeneratorCategory category : GeneratorCategory.values()) {
            ItemStack item = new ItemBuilder(Material.valueOf(category.name()))
                    .setDisplayName(ChatColor.YELLOW + category.name().replace("_", " ").toLowerCase())
                    .setNBT("category", category.name())
                    .build();
            if (selectedCategory == category) {
                item = new ItemBuilder(item)
                        .addEnchant(Enchantment.EFFICIENCY, 1)
                        .addFlags(ItemFlag.HIDE_ENCHANTS)
                        .build();
            }

            gui.setItem(categoryIndex++, item);

        }
        ItemStack allCategory = new ItemBuilder(Material.LIME_CANDLE)
                .setDisplayName(ChatColor.GREEN + "ALL")
                .setNBT("category", "ALL")
                .build();
        if (selectedCategory == null) {
            allCategory = new ItemBuilder(allCategory)
                    .addEnchant(Enchantment.EFFICIENCY, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS)
                    .build();
        }
        gui.setItem(7, allCategory);

        //  **********  Generator Type Items **********

        int typeIndex = 9;
        for (GeneratorType type : types) {
            if (typeIndex >= size) break;
            ItemStack generatorTypeItem = type.buildItem();
            Island island = BentoBox.getInstance().getIslandsManager()
                    .getOwnedIslands(player.getWorld(), player.getUniqueId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (manager.getOwnedTypes(island.getUniqueId()).contains(type.getId())) {
                generatorTypeItem = new ItemBuilder(generatorTypeItem)
                        .addLoreLine("")
                        .addLoreLine("§7Aktifleştirmek için §etıklayın")
                        .setNBT("is_owned", "true")
                        .build();
            } else {
                generatorTypeItem = new ItemBuilder(generatorTypeItem)
                        .addLoreLine("")
                        .addLoreLine("§7Satın almak için §etıklayın")
                        .setNBT("is_owned", "false")
                        .build();
            }

            gui.setItem(typeIndex++, generatorTypeItem);
        }
        player.openInventory(gui);
    }

}
