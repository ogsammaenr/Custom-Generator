package xanth.ogsammaenr.customGenerator.storage;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import xanth.ogsammaenr.customGenerator.CustomGenerator;
import xanth.ogsammaenr.customGenerator.manager.IslandGeneratorManager;
import xanth.ogsammaenr.customGenerator.model.GeneratorCategory;
import xanth.ogsammaenr.customGenerator.model.GeneratorType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IslandGeneratorDAO {
    private final CustomGenerator plugin;
    private final DatabaseConnector connector;

    public IslandGeneratorDAO(CustomGenerator plugin, DatabaseConnector connector) {
        this.plugin = plugin;
        this.connector = connector;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Jeneratör verileri kaydediliyor...");

            saveToJson(plugin.getIslandGeneratorManager());

            clearAllActiveGenerators();
            clearAllOwnedGenerators();

            saveActiveGenerators(plugin.getIslandGeneratorManager());
            saveOwnedGenerators(plugin.getIslandGeneratorManager());

        }, 0L, 5 * 60 * 20L);
    }

    ///     ----------------------------
    ///         YÜKLEME METODLARI
    ///     ----------------------------

    public void loadAll(IslandGeneratorManager manager) {
        loadOwnedGenerators(manager);
        loadActiveGenerators(manager);
    }

    private void loadOwnedGenerators(IslandGeneratorManager manager) {
        String sql = "SELECT island_id, generator_type FROM island_generators";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String islandId = rs.getString("island_id");
                String typeId = rs.getString("generator_type");

                if (manager.isRegistered(typeId)) {
                    manager.addOwnedType(islandId, typeId);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Satın alınmış jeneratörleri yüklerken hata oluştu:");
            e.printStackTrace();
        }
    }

    private void loadActiveGenerators(IslandGeneratorManager manager) {
        String sql = "SELECT island_id, generator_category, generator_type FROM active_generators";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String islandId = rs.getString("island_id");
                String categoryStr = rs.getString("generator_category");
                String typeId = rs.getString("generator_type");

                GeneratorCategory category;
                try {
                    category = GeneratorCategory.valueOf(categoryStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz kategori: " + categoryStr);
                    continue;
                }

                GeneratorType type = manager.getRegisteredType(typeId);
                if (type != null && type.getGeneratorCategory() == category) {
                    manager.setGeneratorType(islandId, typeId);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Aktif jeneratörleri yüklerken hata oluştu:");
            e.printStackTrace();
        }
    }

    /// ----------------------------
    /// KAYDETME METODLARI
    /// ----------------------------

    public void saveOwnedGenerators(IslandGeneratorManager manager) {
        String sql = "REPLACE INTO island_generators (island_id, generator_type) VALUES (?, ?)";

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Map.Entry<String, Set<String>> entry : manager.getOwnedGeneratorTypes().entrySet()) {
                String islandId = entry.getKey();
                for (String typeId : entry.getValue()) {
                    ps.setString(1, islandId);
                    ps.setString(2, typeId);
                    ps.addBatch();
                }
            }

            ps.executeBatch();

        } catch (SQLException e) {
            plugin.getLogger().severe("Satın alınmış jeneratörler kaydedilirken hata oluştu:");
            e.printStackTrace();
        }
    }

    public void saveActiveGenerators(IslandGeneratorManager manager) {
        String sql = "REPLACE INTO active_generators (island_id, generator_category, generator_type) VALUES (?, ?, ?)";

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Map.Entry<String, Map<GeneratorCategory, String>> entry : manager.getActiveGeneratorTypes().entrySet()) {
                String islandId = entry.getKey();
                for (Map.Entry<GeneratorCategory, String> active : entry.getValue().entrySet()) {
                    ps.setString(1, islandId);
                    ps.setString(2, active.getKey().name());
                    ps.setString(3, active.getValue());
                    ps.addBatch();
                }
            }

            ps.executeBatch();

        } catch (SQLException e) {
            plugin.getLogger().severe("Aktif jeneratörler kaydedilirken hata oluştu:");
            e.printStackTrace();
        }
    }

    public void saveToJson(IslandGeneratorManager manager) {
        File file = new File(plugin.getDataFolder(), "last-save.json");

        Map<String, Object> data = new HashMap<>();
        data.put("active", manager.getActiveGeneratorTypes());
        data.put("owned", manager.getOwnedGeneratorTypes());

        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("last-save.json dosyasına yazılamadı: " + e.getMessage());
        }
    }

    /// ---------------------------------------------
    /// Kaldırma Metodları
    /// ---------------------------------------------

    public void clearAllActiveGenerators() {
        String sql = "DELETE FROM active_generators";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Aktif jeneratör verileri temizlenemedi: " + e.getMessage());
        }
    }

    public void clearAllOwnedGenerators() {
        String sql = "DELETE FROM island_generators";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Sahip olunan jeneratör verileri temizlenemedi: " + e.getMessage());
        }
    }
}
