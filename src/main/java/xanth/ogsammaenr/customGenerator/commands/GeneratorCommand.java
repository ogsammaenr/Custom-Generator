package xanth.ogsammaenr.customGenerator.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratorCommand implements CommandExecutor, TabCompleter {
    private final CustomGenerator plugin;
    private final IslandGeneratorManager generatorManager;
    private final CustomCategoryManager categoryManager;
    private final Economy economy;
    private final IslandsManager islandsManager;
    private final IslandUtils islandUtils;
    private final MessagesManager messages;

    private static final List<String> SUB_COMMANDS = List.of("help", "buy", "activate");
    private static final List<String> ADMIN_COMMANDS = List.of("version", "reload", "list");


    public GeneratorCommand(CustomGenerator plugin) {
        this.plugin = plugin;
        this.generatorManager = plugin.getIslandGeneratorManager();
        this.categoryManager = plugin.getCustomCategoryManager();
        this.economy = plugin.getEconomyManager().getEconomy();
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
        this.islandUtils = plugin.getIslandUtils();
        this.messages = plugin.getMessagesManager();
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
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
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
            case "version" -> handleVersionCommand(player, args);
            default -> player.sendMessage(messages.get("commands.general.unknown-command"));
        }

        return true;
    }

    private void handleBuyCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messages.get("commands.buy.usage"));
            return;
        }

        player.sendMessage(generatorManager.getRegisteredTypeString());

        /*      Tip Kontrolü*/
        String typeId = args[1].toLowerCase();
        GeneratorType type = generatorManager.getRegisteredType(typeId);
        if (type == null) {
            player.sendMessage(messages.getFormatted("commands.general.unknown-type", Map.of("generator", typeId)));
            return;
        }

        /*      Dünya Kontrolü      */
        World world = player.getWorld();
        String worldName = world.getName();
        if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
            player.sendMessage(messages.get("commands.general.not-in-island-world"));
        }

        /*      Ada Kontrolü        */
        Island island = islandsManager.getOwnedIslands(world, player.getUniqueId()).stream().findFirst().orElse(null);
        if (island == null) {
            player.sendMessage(messages.get("commands.general.no-island"));
            return;
        }

        /*      Sahiplik Kontrolü       */
        String islandId = island.getUniqueId();
        if (generatorManager.islandOwnsType(islandId, typeId)) {
            player.sendMessage(messages.get("commands.buy.already-owned"));
            return;
        }

        /*      Seviye Kontrolü     */
        long level = islandUtils.getIslandLevel(player.getUniqueId(), worldName);
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
        generatorManager.addOwnedType(islandId, typeId);
        player.sendMessage(messages.getFormatted("commands.buy.success", Map.of("generator", type.getDisplayName(), "price", String.valueOf(price))));
    }

    private void handleActivateCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messages.get("commands.activate.usage"));
            return;
        }

        /*      Dünya Kontrolü      */
        World world = player.getWorld();
        String worldName = world.getName();
        if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
            player.sendMessage(messages.get("commands.general.no-island"));
        }

        /*      Tip Kontrolü        */
        String typeId = args[1].toLowerCase();
        GeneratorType type = generatorManager.getRegisteredType(typeId);
        if (type == null) {
            player.sendMessage(messages.getFormatted("commands.general.unknown-type", Map.of("generator", typeId)));
            return;
        }

        /*      Ada Kontolü     */
        Island island = islandsManager.getOwnedIslands(world, player.getUniqueId()).stream().findFirst().orElse(null);
        if (island == null) {
            player.sendMessage(messages.get("commands.general.no-island"));
            return;
        }

        /*      Sahiplik Kontorlü       */
        String islandId = island.getUniqueId();
        if (!generatorManager.islandOwnsType(islandId, typeId)) {
            player.sendMessage(messages.getFormatted("commands.activate.not-owned", Map.of("generator", typeId)));
            return;
        }

        generatorManager.setGeneratorType(islandId, typeId);

        player.sendMessage(messages.getFormatted("commands.activate.success", Map.of("category", type.getGeneratorCategory().getDisplayName(), "generator", type.getDisplayName())));
    }

    private void handleReloadCommand(Player player, String[] args) {
        if (!player.hasPermission("customgenerator.admin")) {
            player.sendMessage(messages.get("commands.general.no-permission"));
            return;
        }

        plugin.getCustomCategoryLoader().loadCustomCategories();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getTypeLoader().loadGeneratorTypes();

                generatorManager.reloadActiveGeneratorTypes();
            }
        }, 5);

        messages.reload();

        player.sendMessage(messages.get("commands.reload.success"));
    }

    private void handleListCommand(Player player, String[] args) {
        if (!player.hasPermission("customgenerator.admin")) {
            player.sendMessage(messages.get("commands.general.no-permission"));
            return;
        }

        player.sendMessage(messages.get("commands.list.header"));
        for (Map.Entry<String, GeneratorType> entry : generatorManager.getAllRegisteredTypes().entrySet()) {
            GeneratorType type = entry.getValue();
            player.sendMessage(messages.getFormatted("commands.list.entry", Map.of("generator", type.getDisplayName(), "category", type.getGeneratorCategory().getDisplayName())));
        }
    }

    private void handleVersionCommand(Player player, String[] args) {
        if (!player.hasPermission("customgenerator.admin")) {
            player.sendMessage(messages.get("commands.general.no-permission"));
            return;
        }

        player.sendMessage(ChatColor.GRAY + "==========[ " + ChatColor.GOLD + plugin.getName() + ChatColor.GRAY + " ]==========");
        player.sendMessage(ChatColor.YELLOW + "Plugin Version: " + ChatColor.GREEN + plugin.getDescription().getVersion());
        player.sendMessage(ChatColor.YELLOW + "Server Version: " + ChatColor.GREEN + Bukkit.getVersion());
        player.sendMessage(ChatColor.YELLOW + "API Version: " + ChatColor.GREEN + Bukkit.getBukkitVersion());
        player.sendMessage(ChatColor.GRAY + "====================================");

    }

    private void handleHelpCommand(Player player, String[] args) {
        player.sendMessage(messages.get("commands.help.header-top"));
        player.sendMessage(messages.get("commands.help.header-title"));

        List<String> entries = plugin.getMessagesManager().getConfig().getStringList("commands.help.entries");

        for (String line : entries) {
            if ((line.contains("reload") || line.contains("list")) && !player.hasPermission("customgenerator.admin")) {
                continue;
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }

        player.sendMessage(messages.get("commands.help.footer-bottom"));
    }

    private void handleGeneratorCommand(Player player) {
        Location loc = player.getLocation();

        /*      Dünya Kontrolü      */
        World world = player.getWorld();
        String worldName = world.getName();
        if (!worldName.equals("bskyblock_world") && !worldName.equals("bskyblock_world_nether") && !worldName.equals("bskyblock_world_the_end")) {
            player.sendMessage(messages.get("commands.general.not-in-island-world"));
            return;
        }

        /*      Ada Kontrolü        */
        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(loc);
        if (optionalIsland.isEmpty()) {
            player.sendMessage(messages.get("commands.general.no-island"));
            return;
        }

        /*      Sahiplik Kontrolü      */
        Island island = optionalIsland.get();
        if (!island.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(messages.get("commands.general.not-owner"));
            return;
        }

        new GeneratorMenu(plugin).openMenu(player, null, 0);
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
            List<String> commands = new ArrayList<>();
            commands.addAll(SUB_COMMANDS);
            if (sender.hasPermission("customgenerator.admin")) {
                commands.addAll(ADMIN_COMMANDS);
            }
            return StringUtil.copyPartialMatches(args[0], commands, new ArrayList<>());
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
