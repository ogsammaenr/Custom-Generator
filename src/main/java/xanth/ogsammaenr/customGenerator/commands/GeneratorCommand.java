package xanth.ogsammaenr.customGenerator.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.gui.GeneratorMenu;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratorCommand implements CommandExecutor, TabCompleter {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager generatorManager;
    private final Economy economy;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;

    private static final List<String> SUB_COMMANDS = List.of("reload", "list", "info", "buy", "activate");


    public GeneratorCommand(CustomGenerator plugin) {
        this.plugin = plugin;
        this.generatorManager = plugin.getIslandGeneratorManager();
        this.economy = plugin.getEconomyManager().getEconomy();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
    }

    /**
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        if (args.length < 1) {
            handleGeneratorCommand(player);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "buy" -> handleBuyCommand(player, args);
            case "activate" -> handleActivateCommand(player, args);
            case "reload" -> handleReloadCommand(player, args);
            case "list" -> handleListCommand(player, args);
            case "help" -> handleHelpCommand(player, args);
            default -> player.sendMessage(ChatColor.RED + "Bilinmeyen komut: " + sub);
        }

        return true;
    }

    /**
     * @param player Command Sender
     * @param args   Command
     */
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

    /**
     * @param player Command Sender
     * @param args   Command
     */
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
                           ChatColor.GREEN + " jeneratör '" + type.getDisplayName() + ChatColor.GREEN + "' olarak ayarlandı.");
    }

    /**
     * @param player Command sender
     * @param args   Command
     */
    private void handleReloadCommand(Player player, String[] args) {
        if (!player.hasPermission("customgenerator.reload")) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return;
        }

        plugin.reloadConfig(); // config.yml gibi dosyaları yeniden yükler

        player.sendMessage(ChatColor.GREEN + "CustomGenerator başarıyla yeniden yüklendi.");
    }

    /**
     * @param player Command Sender
     * @param args   Command
     */
    private void handleListCommand(Player player, String[] args) {
        player.sendMessage(ChatColor.GOLD + "Kayıtlı Jeneratör Tipleri:");
        for (Map.Entry<String, GeneratorType> entry : generatorManager.getAllRegisteredTypes().entrySet()) {
            GeneratorType type = entry.getValue();
            player.sendMessage(ChatColor.YELLOW + "- " + type.getId() + ChatColor.GRAY +
                               " (Kategori: " + type.getGeneratorCategory().name() + ")");
        }
    }

    /**
     * @param player Command Sender
     * @param args   Command
     */
    private void handleHelpCommand(Player player, String[] args) {
        player.sendMessage("§8§m----------------------------------------");
        player.sendMessage("§b§lCustomGenerator &7- Help Menu");
        player.sendMessage("&e/generator buy <generatorType> &7- jeneratör tipini satın alır");
        player.sendMessage("&e/generator activate <generatorType> &7- jeneratör tipini aktifleştirir");
        player.sendMessage("&e/generator list &7- sunucuda tanımli olan jeneratör tiplerini gösterir");
        player.sendMessage("&e/generator help &7- bu sayfayı açar");
        if (player.hasPermission("customgenerator.admin")) {
            player.sendMessage("&e/generator reload &7- config dosyalarını yeniden yükler");
        }
        player.sendMessage("§8§m----------------------------------------");
    }

    private void handleGeneratorCommand(Player player) {
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

        new GeneratorMenu(plugin).openMenu(player, null);
    }

    /**
     * <h3>Command Tab Handler</h3>
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     * @return Completed Commands
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUB_COMMANDS, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equals("buy") && sender instanceof Player player) {
                World world = player.getWorld();
                String worldName = world.getName();
                if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
                    return Collections.emptyList();
                }
                Island island = islandsManager.getOwnedIslands(world, player.getUniqueId()).stream().findFirst().orElse(null);

                Set<String> ownedTypes = generatorManager.getOwnedTypes(island.getUniqueId());

                return generatorManager.getAllRegisteredTypes().values().stream()
                        .filter(type -> !ownedTypes.contains(type.getId()))
                        .map(GeneratorType::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equals("activate") && sender instanceof Player player) {
                Island island = islandsManager.getOwnedIslands(Bukkit.getWorld("bskyblock_world"), player.getUniqueId()).stream().findFirst().orElse(null);

                return generatorManager.getOwnedTypes(island.getUniqueId()).stream().toList();
            }
        }

        return Collections.emptyList();
    }
}
