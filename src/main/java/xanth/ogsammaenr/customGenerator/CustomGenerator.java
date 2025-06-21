package xanth.ogsammaenr.customGenerator;

import org.bukkit.plugin.java.JavaPlugin;
import xanth.ogsammaenr.customGenerator.commands.GeneratorCommand;
import xanth.ogsammaenr.customGenerator.manager.EconomyManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.storage.GeneratorTypeLoader;
import xanth.ogsammaenr.customGenerator.util.IslandUtils;

public final class CustomGenerator extends JavaPlugin {
    private CustomGenerator instance;

    private EconomyManager economyManager;
    private IslandGeneratorManager islandGeneratorManager;
    private GeneratorTypeLoader typeLoader;
    private IslandUtils islandUtils;

    @Override
    public void onEnable() {
        this.instance = this;

        this.islandUtils = new IslandUtils();

        saveDefaultConfig();

        ///========== Manager sınıfları ==========
        economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault ekonomisi bulunamadı! Plugin kapatılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        islandGeneratorManager = new IslandGeneratorManager(this);

        typeLoader = new GeneratorTypeLoader(this);
        typeLoader.loadGeneratorTypes();

        ///========== Komut kayıtları ==========
        getCommand("generator").setExecutor(new GeneratorCommand(this));

//        ///========== listener Kayıtları =========
//        getServer().getPluginManager().registerEvents(new listeners.BlockBreakListener(this), this);

        getLogger().info("***** CustomGenerator is enabled *****");
    }

    @Override
    public void onDisable() {


        getLogger().info("***** CustomGenerator is disabled *****");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public IslandGeneratorManager getIslandGeneratorManager() {
        return islandGeneratorManager;
    }

    public CustomGenerator getInstance() {
        return instance;
    }

    public IslandUtils getIslandUtils() {
        return islandUtils;
    }

}
