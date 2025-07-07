package xanth.ogsammaenr.customGenerator;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xanth.ogsammaenr.customGenerator.commands.GeneratorCommand;
import xanth.ogsammaenr.customGenerator.listeners.FertilizeListener;
import xanth.ogsammaenr.customGenerator.listeners.GeneratorFlowListener;
import xanth.ogsammaenr.customGenerator.listeners.GeneratorListener;
import xanth.ogsammaenr.customGenerator.listeners.InventoryClickListener;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.EconomyManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.storage.DatabaseConnector;
import xanth.ogsammaenr.customGenerator.storage.GeneratorTypeLoader;
import xanth.ogsammaenr.customGenerator.storage.IslandGeneratorDAO;
import xanth.ogsammaenr.customGenerator.storage.SQLiteConnector;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

public final class CustomGenerator extends JavaPlugin {
    private static CustomGenerator instance;

    private EconomyManager economyManager;
    private IslandGeneratorManager islandGeneratorManager;
    private MessagesManager messagesManager;
    private CustomCategoryManager customCategoryManager;

    private GeneratorTypeLoader typeLoader;
    private DatabaseConnector databaseConnector;
    private IslandGeneratorDAO islandGeneratorDAO;

    private IslandUtils islandUtils;

    private GeneratorListener generatorListener;
    private GeneratorFlowListener generatorFlowListener;
    private InventoryClickListener inventoryClickListener;
    private FertilizeListener fertilizeListener;
    private PluginManager pm;

    @Override
    public void onEnable() {
        this.instance = this;

        this.islandUtils = new IslandUtils();

        this.databaseConnector = new SQLiteConnector(this);
        this.islandGeneratorDAO = new IslandGeneratorDAO(this, databaseConnector);

        this.pm = getServer().getPluginManager();

        ///========== Manager sınıfları ==========
        economyManager = new EconomyManager(this);
        islandGeneratorManager = new IslandGeneratorManager(this, islandGeneratorDAO);
        messagesManager = new MessagesManager(this);
        customCategoryManager = new CustomCategoryManager();

        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault ekonomisi bulunamadı! Plugin kapatılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ///========== Veri Yükleme ==========
        this.typeLoader = new GeneratorTypeLoader(this);

        typeLoader.loadGeneratorTypes();
        islandGeneratorDAO.loadAll(islandGeneratorManager);

        ///========== Komut kayıtları ==========
        getCommand("generator").setExecutor(new GeneratorCommand(this));

        ///========== listener Kayıtları =========
        generatorFlowListener = new GeneratorFlowListener();
        fertilizeListener = new FertilizeListener();
        inventoryClickListener = new InventoryClickListener(this);

        pm.registerEvents(generatorFlowListener, this);
        pm.registerEvents(inventoryClickListener, this);
        pm.registerEvents(fertilizeListener, this);

        getLogger().info("***** CustomGenerator is enabled *****");
    }

    @Override
    public void onDisable() {
        islandGeneratorDAO.saveToJson(islandGeneratorManager);

        islandGeneratorDAO.saveOwnedGenerators(islandGeneratorManager);
        islandGeneratorDAO.saveActiveGenerators(islandGeneratorManager);

        databaseConnector.close();


        getLogger().info("***** CustomGenerator is disabled *****");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public IslandGeneratorManager getIslandGeneratorManager() {
        return islandGeneratorManager;
    }

    public static CustomGenerator getInstance() {
        return instance;
    }

    public IslandUtils getIslandUtils() {
        return islandUtils;
    }

    public GeneratorTypeLoader getTypeLoader() {
        return typeLoader;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public CustomCategoryManager getCustomCategoryManager() {
        return customCategoryManager;
    }
}
