package xanth.ogsammaenr.customGenerator.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
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
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

public class InventoryClickListener implements Listener {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager manager;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;
    private final Economy economy;

    public InventoryClickListener(CustomGenerator plugin) {
        this.plugin = plugin;
        this.manager = plugin.getIslandGeneratorManager();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
        this.economy = plugin.getEconomyManager().getEconomy();
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().startsWith("Ada Jeneratörleri")) {
            System.out.println("buraya girildi ************************");
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String categoryId = getStringTag(clicked, "category");
        String is_owned = getStringTag(clicked, "is_owned");
        String generator_id = getStringTag(clicked, "generator_id");
        if (categoryId != null) {
            if (categoryId.equals("ALL")) {
                new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, null);
                return;
            }

            new GeneratorMenu(CustomGenerator.getInstance()).openMenu(player, GeneratorCategory.valueOf(categoryId));
        } else if (generator_id != null) {
            GeneratorType type = manager.getRegisteredType(generator_id);
            Island island = islandsManager.getOwnedIslands(player.getWorld(), player.getUniqueId()).stream().findFirst().orElse(null);

            if (is_owned.equals("true")) {
                manager.setGeneratorType(island.getUniqueId(), generator_id);

                player.sendMessage(ChatColor.GREEN + "Aktif " + ChatColor.GOLD + type.getGeneratorCategory().name() +
                                   ChatColor.GREEN + " jeneratör '" + type.getDisplayName() + "' olarak ayarlandı.");
            } else {
                /*      Seviye Kontrolü     */
                long level = islandUtils.getIslandLevel(player.getUniqueId(), player.getWorld().getName());
                if (level < type.getRequiredIslandLevel()) {
                    player.sendMessage(ChatColor.RED + "Bu jeneratörü satın almak için gereken ada seviyesi: " + type.getRequiredIslandLevel());
                    return;
                }

                /*      Para Kontrolü       */
                double price = type.getPrice();
                if (!economy.has(player, price)) {
                    player.sendMessage(ChatColor.RED + "Bu jeneratörü satın almak için yeterli paranız yok. Gerekli: " + price);
                    return;
                }

                economy.withdrawPlayer(player, price);
                manager.addOwnedType(island.getUniqueId(), generator_id);
                player.sendMessage(ChatColor.GREEN + "Başarıyla '" + type.getDisplayName() + "' jeneratörünü satın aldınız. " + ChatColor.YELLOW + type.getPrice());
            }
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
