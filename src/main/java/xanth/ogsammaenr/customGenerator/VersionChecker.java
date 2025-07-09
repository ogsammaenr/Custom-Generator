package xanth.ogsammaenr.customGenerator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker implements Listener {
    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String currentVersion = "";

    public VersionChecker(CustomGenerator plugin) {
        checkModrinthVersion(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) return;
        if (hasNewVersion()) {
            player.sendMessage("§7[ §b§lCustomGenerator §7] §aA new version is available! §7[§e" + latestVersion + "§7]");
            player.sendMessage("§7[ §b§lCustomGenerator §7] §aCurrent version : " + currentVersion);
            player.sendMessage("§7[ §b§lCustomGenerator §7] §aDownload : https://modrinth.com/plugin/customgenerator/version/" + latestVersion);
        }

    }

    public void checkModrinthVersion(JavaPlugin plugin) {
        String url = "https://api.modrinth.com/v2/project/customgenerator/version";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonText.append(line);
                }
                reader.close();

                JSONArray versions = new JSONArray(jsonText.toString());
                if (versions.length() == 0) return;

                JSONObject latest = versions.getJSONObject(0);
                latestVersion = latest.getString("version_number");

                currentVersion = plugin.getDescription().getVersion();

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    plugin.getLogger().warning("A new version is available! [" + latestVersion + "]");
                    plugin.getLogger().warning("Current version : " + currentVersion);
                    plugin.getLogger().warning("Download : https://modrinth.com/plugin/customgenerator/version/" + latestVersion);
                    updateAvailable = true;
                } else {
                    plugin.getLogger().info("Plugin is up to date  (v" + currentVersion + ")");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Modrinth Version Check Failed: " + e.getMessage());
            }
        });
    }

    public boolean hasNewVersion() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
