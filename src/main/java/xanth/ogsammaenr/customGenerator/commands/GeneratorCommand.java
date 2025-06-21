package xanth.ogsammaenr.customGenerator.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

import java.util.List;

public class GeneratorCommand implements CommandExecutor, TabCompleter {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager generatorManager;
    private final Economy economy;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;


    public GeneratorCommand(CustomGenerator plugin) {
        this.plugin = plugin;
        this.generatorManager = plugin.getIslandGeneratorManager();
        this.economy = plugin.getEconomyManager().getEconomy();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Kullanım: /generator <buy|activate> <type>");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "buy" -> handleBuyCommand(player, args);
            case "activate" -> handleActivateCommand(player, args);
            default -> player.sendMessage(ChatColor.RED + "Bilinmeyen komut: " + sub);
        }

        return true;
    }

    private void handleBuyCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /generator buy <type>");
            return;
        }

        player.sendMessage(generatorManager.getRegisteredTypeString());

        String typeId = args[1].toLowerCase();
        GeneratorType type = generatorManager.getRegisteredType(typeId);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen jeneratör tipi: " + typeId);
            return;
        }

        /*      Dünya Kontrolü      */
        World world = player.getWorld();
        String worldName = world.getName();
        if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
            player.sendMessage(ChatColor.YELLOW + "adalar dünyasında değilsin");
        }

        /*      Ada Kontrolü        */
        Island island = islandsManager.getOwnedIslands(world, player.getUniqueId()).stream().findFirst().orElse(null);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Bir adaya sahip değilsiniz.");
            return;
        }

        /*      Sahiplik Kontrolü       */
        String islandId = island.getUniqueId();
        if (generatorManager.islandOwnsType(islandId, typeId)) {
            player.sendMessage(ChatColor.YELLOW + "Zaten bu jeneratör tipine sahipsiniz.");
            return;
        }

        /*      Seviye Kontrolü     */
        long level = islandUtils.getIslandLevel(player.getUniqueId(), worldName);
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
        generatorManager.addOwnedType(islandId, typeId);
        player.sendMessage(ChatColor.GREEN + "Başarıyla '" + type.getDisplayName() + "' jeneratörünü satın aldınız. " + ChatColor.YELLOW + type.getPrice());
    }

    private void handleActivateCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /generator activate <type>");
            return;
        }

        /*      Dünya Kontrolü      */
        World world = player.getWorld();
        String worldName = world.getName();
        if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
            player.sendMessage(ChatColor.YELLOW + "adalar dünyasında değilsin");
        }

        /*      Tip Kontrolü        */
        String typeId = args[1].toLowerCase();
        GeneratorType type = generatorManager.getRegisteredType(typeId);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen jeneratör tipi: " + typeId);
            return;
        }

        /*      Ada Kontolü     */
        Island island = islandsManager.getOwnedIslands(world, player.getUniqueId()).stream().findFirst().orElse(null);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Bir adaya sahip değilsiniz.");
            return;
        }

        /*      Sahiplik Kontorlü       */
        String islandId = island.getUniqueId();
        if (!generatorManager.islandOwnsType(islandId, typeId)) {
            player.sendMessage(ChatColor.RED + "Bu jeneratör tipine sahip değilsiniz.");
            return;
        }
        
        generatorManager.setGeneratorType(islandId, typeId);

        player.sendMessage(ChatColor.GREEN + "Aktif " + ChatColor.GOLD + type.getGeneratorCategory().name() +
                ChatColor.GREEN + " jeneratör '" + type.getDisplayName() + "' olarak ayarlandı.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // İsteğe bağlı olarak otomatik tamamlama eklenebilir
        return null;
    }
}
