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
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

import java.util.Map;
import java.util.Optional;

public class InventoryClickListener implements Listener {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;
    private final Economy economy;
    private final MessagesManager messages;

    public InventoryClickListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
        this.economy = plugin.getEconomyManager().getEconomy();
        this.messages = plugin.getMessagesManager();
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().startsWith(messages.get("gui.title"))) {
            return;
        }

        e.setCancelled(true);
        String[] parts = e.getView().getTitle().split(" ");
        String selectedCategoryId = parts[parts.length - 1];
        GeneratorCategory selectedCategory = selectedCategoryId.equals("ALL") ? null : GeneratorCategory.valueOf(selectedCategoryId);


        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String categoryId = getStringTag(clicked, "category");
        String is_owned = getStringTag(clicked, "is_owned");
        String generator_id = getStringTag(clicked, "generator_id");
        String deactivate = getStringTag(clicked, "deactivate");
        if (categoryId != null) {
            if (categoryId.equals("ALL")) {
                new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, null);
                return;
            }

            new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, GeneratorCategory.valueOf(categoryId));
        } else if (generator_id != null) {
            GeneratorType type = manager.getRegisteredType(generator_id);

            Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
            if (optionalIsland.isEmpty()) {
                player.sendMessage(messages.get("commands.general.no-island"));
                return;
            }
            Island island = optionalIsland.get();
            if (is_owned.equals("true")) {
                manager.setGeneratorType(island.getUniqueId(), generator_id);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, selectedCategory);
                }, 3L);
                player.sendMessage(messages.getFormatted("commands.activate.success", Map.of("category", type.getGeneratorCategory().getDisplayName(), "generator", type.getDisplayName())));

            } else {
                if (manager.islandOwnsType(island.getUniqueId(), generator_id)) {
                    return;
                }
                /*      Seviye Kontrolü     */
                long level = islandUtils.getIslandLevel(player.getUniqueId(), player.getWorld().getName());
                if (level < type.getRequiredIslandLevel()) {
                    player.sendMessage(messages.getFormatted("commands.buy.not-enough-level", Map.of("required_level", String.valueOf(type.getRequiredIslandLevel()))));
                    return;
                }

                /*      Para Kontrolü       */
                double price = type.getPrice();
                if (!economy.has(player, price)) {
                    player.sendMessage(messages.getFormatted("commands.buy.not-enough-money", Map.of("price", String.valueOf(price))));
                    return;
                }

                economy.withdrawPlayer(player, price);
                manager.addOwnedType(island.getUniqueId(), generator_id);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, selectedCategory);
                }, 3L);

                player.sendMessage(messages.getFormatted("commands.buy.success", Map.of("generator", type.getDisplayName(), "price", String.valueOf(price))));
            }
        } else if (deactivate != null) {
            Island island = islandsManager.getOwnedIslands(player.getWorld(), player.getUniqueId()).stream().findFirst().orElse(null);

            manager.removeGeneratorType(island.getUniqueId(), GeneratorCategory.valueOf(deactivate));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, GeneratorCategory.valueOf(deactivate));
            }, 3L);

            player.sendMessage(messages.getFormatted("gui.deactivate", Map.of("category", GeneratorCategory.valueOf(deactivate).getDisplayName())));


        }
    }

    private static String getStringTag(ItemStack item, String key) {
        CustomGenerator plugin = CustomGenerator.getInstance();
        if (item == null || item.getItemMeta() == null) return null;

        var meta = item.getItemMeta();
        var container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);

        if (container.has(namespacedKey, PersistentDataType.STRING)) {
            return container.get(namespacedKey, PersistentDataType.STRING);
        }
        return null;
    }
}
