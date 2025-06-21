package xanth.ogsammaenr.customGenerator.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import xanth.ogsammaenr.customGenerator.CustomGenerator;

public class EconomyManager {
    private final CustomGenerator plugin;
    private Economy economy;

    public EconomyManager(CustomGenerator plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault bulunamadı!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);

        if (rsp == null) {
            plugin.getLogger().severe("Economy servisi bulunamadı!");
            return false;
        }

        economy = rsp.getProvider();

        if (economy == null) {
            plugin.getLogger().severe("Economy sağlayıcısı null!");
            return false;
        }

        plugin.getLogger().info("Vault ekonomisi başarıyla bağlandı: " + economy.getName());
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }
}
