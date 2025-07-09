package xanth.ogsammaenr.customGenerator;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xanth.ogsammaenr.customGenerator.commands.GeneratorCommand;
import xanth.ogsammaenr.customGenerator.listeners.*;
import xanth.ogsammaenr.customGenerator.manager.CustomCategoryManager;
import xanth.ogsammaenr.customGenerator.manager.EconomyManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.manager.MessagesManager;
import xanth.ogsammaenr.customGenerator.storage.*;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

public final class CustomGenerator extends JavaPlugin {
    private static CustomGenerator instance;

    private EconomyManager economyManager;
    private IslandGeneratorManager islandGeneratorManager;
    private MessagesManager messagesManager;
    private CustomCategoryManager customCategoryManager;

    private GeneratorTypeLoader typeLoader;
    private CustomCategoryLoader customCategoryLoader;
    private DatabaseConnector databaseConnector;
    private IslandGeneratorDAO islandGeneratorDAO;

    private IslandUtils islandUtils;

    private CustomGeneratorListener customGeneratorListener;
    private GeneratorFlowListener generatorFlowListener;
    private FertilizeListener fertilizeListener;
    private GeneratorListener generatorListener;
    private CustomGeneratorPlaceListener customGeneratorPlaceListener;
    private InventoryClickListener inventoryClickListener;
    private VersionChecker versionChecker;

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
        customCategoryManager = new CustomCategoryManager();
        islandGeneratorManager = new IslandGeneratorManager(this, islandGeneratorDAO);
        messagesManager = new MessagesManager(this);

        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault ekonomisi bulunamadı! Plugin kapatılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ///========== Veri Yükleme ==========
        this.typeLoader = new GeneratorTypeLoader(this);
        this.customCategoryLoader = new CustomCategoryLoader(this);

        customCategoryLoader.loadCustomCategories();
        typeLoader.loadGeneratorTypes();
        islandGeneratorDAO.loadAll(islandGeneratorManager, customCategoryManager);

        ///========== Komut kayıtları ==========
        getCommand("generator").setExecutor(new GeneratorCommand(this));

        ///========== listener Kayıtları =========
//        generatorFlowListener = new GeneratorFlowListener();
//        fertilizeListener = new FertilizeListener();
//        customGeneratorListener = new CustomGeneratorListener(this);
        inventoryClickListener = new InventoryClickListener(this);
        generatorListener = new GeneratorListener(this);
        customGeneratorPlaceListener = new CustomGeneratorPlaceListener(this);
        versionChecker = new VersionChecker(this);

//        pm.registerEvents(generatorFlowListener, this);
//        pm.registerEvents(fertilizeListener, this);
//        pm.registerEvents(customGeneratorListener, this);
        pm.registerEvents(inventoryClickListener, this);
        pm.registerEvents(generatorListener, this);
        pm.registerEvents(customGeneratorPlaceListener, this);
        pm.registerEvents(versionChecker, this);

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

    public CustomCategoryLoader getCustomCategoryLoader() {
        return customCategoryLoader;
    }
}
