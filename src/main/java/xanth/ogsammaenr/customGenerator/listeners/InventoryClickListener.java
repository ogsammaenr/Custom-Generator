package xanth.ogsammaenr.customGenerator.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.gui.GeneratorMenu;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.model.IGeneratorCategory;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Listens for inventory click events within the custom generator GUI and handles
 * category navigation, generator purchase, activation, and deactivation logic.
 */
public class InventoryClickListener implements Listener {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;
    private final Economy economy;
    private final MessagesManager messages;
    private final CustomCategoryManager customCatManager;

    /**
     * Initializes the listener with required managers and utilities.
     *
     * @param plugin main plugin instance
     */
    public InventoryClickListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
        this.economy = plugin.getEconomyManager().getEconomy();
        this.messages = plugin.getMessagesManager();
        this.customCatManager = plugin.getCustomCategoryManager();
    }

    /**
     * Handles clicking inside the generator menu. Cancels the click and interprets
     * NBT tags to navigate categories or process generator buy/activate/deactivate actions.
     *
     * @param e the inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().startsWith(messages.get("gui.title"))) {
            return;
        }

        e.setCancelled(true);
        String[] parts = e.getView().getTitle().split(" ");
        String selectedCategoryId = parts[parts.length - 1];

        // Determine selected category (built-in, custom, or all)
        IGeneratorCategory selectedCategory;
        if (selectedCategoryId.equals("ALL")) {
            selectedCategory = null;
        } else if (selectedCategoryId.equals("CUSTOM")) {
            selectedCategory = customCatManager.getAllCategories().stream().findFirst().orElse(null);
        } else {
            selectedCategory = GeneratorCategory.valueOf(selectedCategoryId);
        }

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String categoryId = getStringTag(clicked, "category");
        String isOwned = getStringTag(clicked, "is_owned");
        String generatorId = getStringTag(clicked, "generator_id");
        String deactivate = getStringTag(clicked, "deactivate");

        // Category navigation
        if (categoryId != null) {
            if (categoryId.equals("ALL")) {
                new GeneratorMenu(plugin).openMenu(player, null);
            } else if (categoryId.equals("CUSTOM")) {
                new GeneratorMenu(plugin).openMenu(player,
                        customCatManager.getAllCategories().stream().findFirst().orElse(null));
            } else {
                new GeneratorMenu(plugin).openMenu(player, GeneratorCategory.valueOf(categoryId));
            }
            return;
        }

        // Generator purchase / activation / deactivation
        if (deactivate != null) {
            handleDeactivate(player, selectedCategory, deactivate);
        } else if (generatorId != null) {
            handleGeneratorAction(player, selectedCategory, generatorId, isOwned);
        }
    }

    /**
     * Processes buying or activating a generator based on NBT flags.
     */
    private void handleGeneratorAction(Player player, IGeneratorCategory selectedCategory,
                                       String generatorId, String isOwned) {
        GeneratorType type = manager.getRegisteredType(generatorId);
        Optional<Island> opt = islandsManager.getIslandAt(player.getLocation());
        if (opt.isEmpty()) {
            player.sendMessage(messages.get("commands.general.no-island"));
            return;
        }
        Island island = opt.get();

        if (isOwned.equals("true")) {
            // Activate generator
            manager.setGeneratorType(island.getUniqueId(), generatorId);
            reshowMenu(player, selectedCategory);
            player.sendMessage(messages.getFormatted("commands.activate.success",
                    Map.of("category", type.getGeneratorCategory().getDisplayName(),
                            "generator", type.getDisplayName())));
        } else {
            // Buy generator
            attemptPurchase(player, island.getUniqueId(), type, selectedCategory);
        }
    }

    /**
     * Attempts to purchase a generator, performing level and economy checks.
     */
    private void attemptPurchase(Player player, String islandId,
                                 GeneratorType type, IGeneratorCategory selectedCategory) {
        if (manager.islandOwnsType(islandId, type.getId())) return;
        long level = islandUtils.getIslandLevel(player.getUniqueId(), player.getWorld().getName());
        if (level < type.getRequiredIslandLevel()) {
            player.sendMessage(messages.getFormatted("commands.buy.not-enough-level",
                    Map.of("required_level", String.valueOf(type.getRequiredIslandLevel()))));
            return;
        }
        double price = type.getPrice();
        if (!economy.has(player, price)) {
            player.sendMessage(messages.getFormatted("commands.buy.not-enough-money",
                    Map.of("price", String.valueOf(price))));
            return;
        }
        economy.withdrawPlayer(player, price);
        manager.addOwnedType(islandId, type.getId());
        reshowMenu(player, selectedCategory);
        player.sendMessage(messages.getFormatted("commands.buy.success",
                Map.of("generator", type.getDisplayName(), "price", String.valueOf(price))));
    }

    /**
     * Handles deactivation of a generator type.
     */
    private void handleDeactivate(Player player, IGeneratorCategory selectedCategory,
                                  String deactivate) {
        IGeneratorCategory category = null;
        try {
            category = GeneratorCategory.valueOf(deactivate);
        } catch (IllegalArgumentException ex) {
            if (customCatManager.getCategoryMap().containsKey(deactivate)) {
                category = customCatManager.getCategoryById(deactivate);
            }
        }
        manager.removeGeneratorType(
                islandsManager.getIslandAt(player.getLocation()).get().getUniqueId(),
                category);
        reshowMenu(player, selectedCategory);
        player.sendMessage(messages.getFormatted("gui.deactivate",
                Map.of("category", category.getDisplayName())));
    }

    /**
     * Utility to reopen the generator menu after a short delay.
     */
    private void reshowMenu(Player player, IGeneratorCategory selectedCategory) {
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                new GeneratorMenu(plugin).openMenu(player, selectedCategory), 3L);
    }

    /**
     * Reads a string tag from the item's persistent data container.
     *
     * @param item the itemstack containing the tag
     * @param key  the NBT key to read
     * @return the stored string, or null if absent
     */
    private static String getStringTag(ItemStack item, String key) {
        CustomGenerator plugin = CustomGenerator.getInstance();
        if (item == null || item.getItemMeta() == null) return null;
        var container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey ns = new NamespacedKey(plugin, key);
        return container.has(ns, PersistentDataType.STRING)
                ? container.get(ns, PersistentDataType.STRING)
                : null;
    }
}
