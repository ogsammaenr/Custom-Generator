package xanth.ogsammaenr.customGenerator.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.model.CustomGeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.model.IGeneratorCategory;
import xanth.ogsammaenr.customGenerator.util.ItemBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles displaying the generator selection GUI.
 * Supports filtering built-in and custom categories, and shows purchase/activation statuses.
 */
public class GeneratorMenu {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;
    private final CustomCategoryManager categoryManager;
    private final MessagesManager messages;

    private static final int ITEMS_PER_PAGE = 27;

    /**
     * Constructs the menu with necessary plugin managers.
     *
     * @param plugin main plugin instance
     */
    public GeneratorMenu(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
        this.messages = plugin.getMessagesManager();
        this.categoryManager = plugin.getCustomCategoryManager();
    }

    /**
     * Opens the generator GUI for a player.
     *
     * @param player           the player to open the menu for
     * @param selectedCategory optional category to filter, null for all
     */
    public void openMenu(Player player, @Nullable IGeneratorCategory selectedCategory, int page) {
        // Build filtered, sorted list of generator types
        List<GeneratorType> types = manager.getAllRegisteredTypes().values().stream()
                .filter(type -> selectedCategory == null
                                || type.getGeneratorCategory() == selectedCategory
                                || (type.getGeneratorCategory() instanceof CustomGeneratorCategory
                                    && selectedCategory instanceof CustomGeneratorCategory))
                .sorted(Comparator.comparingInt(GeneratorType::getPriority))
                .collect(Collectors.toList());

        int size = 5 * 9;
        String title = determineTitle(selectedCategory);
        Inventory gui = Bukkit.createInventory(null, size,
                messages.get("gui.title") + "§7 - " + title);

        fillBackground(gui, size);
        addCategoryButtons(gui, selectedCategory);
        addCustomCategoryButton(gui, selectedCategory);
        populateTypes(gui, player, selectedCategory, types, page);

        player.openInventory(gui);
    }

    /**
     * Determines the menu title based on selection.
     */
    private String determineTitle(@Nullable IGeneratorCategory selectedCategory) {
        if (selectedCategory instanceof CustomGeneratorCategory) {
            return "CUSTOM";
        } else if (selectedCategory == null) {
            return "ALL";
        }
        return selectedCategory.getId();
    }

    /**
     * Fills GUI background with filler items.
     */
    private void fillBackground(Inventory gui, int size) {
        ItemStack lineFiller = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7").build();
        ItemStack backgroundFiller = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§7").build();

        for (int i = 0; i < size; i++) gui.setItem(i, backgroundFiller);

        for (int i = 0; i < 9; i++) gui.setItem(i, lineFiller);

        for (int i = size - 9; i < size; i++) gui.setItem(i, lineFiller);
    }

    /**
     * Adds built-in category buttons.
     */
    private void addCategoryButtons(Inventory gui, IGeneratorCategory selectedCategory) {
        int index = 2;
        for (GeneratorCategory category : GeneratorCategory.values()) {
            ItemStack item = new ItemBuilder(Material.valueOf(category.name()))
                    .setDisplayName(ChatColor.YELLOW + category.getDisplayName())
                    .setNBT("category", category.name()).build();
            if (selectedCategory == category) {
                item = new ItemBuilder(item)
                        .addEnchant(Enchantment.MENDING, 1)
                        .addFlags(ItemFlag.HIDE_ENCHANTS).build();
            }
            gui.setItem(index++, item);
        }
        // "All" button
        ItemStack all = new ItemBuilder(Material.LIME_CANDLE)
                .setDisplayName(messages.get("generator-category.all"))
                .setNBT("category", "ALL").build();
        if (selectedCategory == null) {
            all = new ItemBuilder(all)
                    .addEnchant(Enchantment.MENDING, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS).build();
        }
        gui.setItem(6, all);
    }

    /**
     * Adds the custom categories button if any exist.
     */
    private void addCustomCategoryButton(Inventory gui, IGeneratorCategory selectedCategory) {
        if (categoryManager.getAllCategories().isEmpty()) return;
        ItemStack custom = new ItemBuilder(Material.MAGENTA_CANDLE)
                .setDisplayName(messages.get("generator-category.custom"))
                .setNBT("category", "CUSTOM").build();
        if (selectedCategory instanceof CustomGeneratorCategory) {
            custom = new ItemBuilder(custom)
                    .addEnchant(Enchantment.MENDING, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS).build();
        }
        gui.setItem(40, custom);
    }

    /**
     * Populates generator type items in the GUI.
     */
    private void populateTypes(Inventory gui, Player player,
                               IGeneratorCategory selectedCategory,
                               List<GeneratorType> types, int page) {
        Optional<Island> opt = BentoBox.getInstance().getIslandsManager()
                .getIslandAt(player.getLocation());
        if (opt.isEmpty()) {
            player.sendMessage(messages.get("commands.general.no-island"));
            return;
        }
        String islandId = opt.get().getUniqueId();
        Set<String> owned = manager.getOwnedTypes(islandId);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, types.size());
        int guiIndex = 9;

        for (int i = startIndex; i < endIndex; i++) {
            GeneratorType type = types.get(i);
            ItemBuilder builder = new ItemBuilder(type.buildItem());
            boolean isOwned = owned.contains(type.getId());
            boolean isActive = manager.isGeneratorTypeActive(islandId, type.getId());

            builder.addLoreLine("");
            if (isActive) {
                builder.addLoreLine(messages.get("gui.click-to-deactivate"));
                builder.setNBT("is_owned", "active");
                builder.setNBT("deactivate", type.getGeneratorCategory().getId());
            } else if (isOwned) {
                builder.addLoreLine(messages.get("gui.click-to-activate"));
                builder.setNBT("is_owned", "true");
            } else {
                builder.addLoreLine(messages.get("gui.click-to-buy"));
                builder.setNBT("is_owned", "false");
            }

            if (isActive) builder.addEnchant(Enchantment.MENDING, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS);

            builder.setNBT("current-page", String.valueOf(page));

            gui.setItem(guiIndex++, builder.build());
        }

        addPaginationButtons(gui, page, types.size());
    }

    private void addPaginationButtons(Inventory gui, int currentPage, int totalItems) {
        int totalPages = (int) Math.ceil(totalItems / (double) ITEMS_PER_PAGE);

        // Show "Previous Page" only if we're not on the first page
        if (currentPage > 0) {
            ItemStack prevPage = new ItemBuilder(Material.ARROW)
                    .setDisplayName(messages.get("gui.previous-page"))
                    .setNBT("page", String.valueOf(currentPage - 1))
                    .build();
            gui.setItem(36, prevPage);
        }

        // Show "Next Page" only if there are more pages ahead
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemBuilder(Material.ARROW)
                    .setDisplayName(messages.get("gui.next-page"))
                    .setNBT("page", String.valueOf(currentPage + 1))
                    .build();
            gui.setItem(44, nextPage);
        }
    }

}
