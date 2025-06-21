package xanth.ogsammaenr.customGenerator;

import org.bukkit.plugin.java.JavaPlugin;
import xanth.ogsammaenr.customGenerator.manager.EconomyManager;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.storage.GeneratorTypeLoader;

public final class CustomGenerator extends JavaPlugin {
    private CustomGenerator instance;

    private EconomyManager economyManager;
    private IslandGeneratorManager generatorManager;
    private GeneratorTypeLoader typeLoader;

    @Override
    public void onEnable() {
        this.instance = this;

        saveDefaultConfig();

        ///========== Manager sınıfları ==========
        economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault ekonomisi bulunamadı! Plugin kapatılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        generatorManager = new IslandGeneratorManager(this);

        typeLoader = new GeneratorTypeLoader(this);
        typeLoader.loadGeneratorTypes();

//        ///========== Komut kayıtları ==========
//        getCommand("generator").setExecutor(new commands.GeneratorCommand(this));
//
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
        return generatorManager;
    }

    public CustomGenerator getInstance() {
        return instance;
    }
}
