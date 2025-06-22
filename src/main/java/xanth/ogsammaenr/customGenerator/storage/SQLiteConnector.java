package xanth.ogsammaenr.customGenerator.storage;

import xanth.ogsammaenr.customGenerator.CustomGenerator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnector implements DatabaseConnector {
    private final CustomGenerator plugin;
    private Connection connection;

    public SQLiteConnector(CustomGenerator plugin) {
        this.plugin = plugin;
        connect();
        initializeTables();
    }

    @Override
    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "generators.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Veritabanına bağlanırken hata oluştu!");
        }
    }

    /**
     * @return {@code Connection}
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            connect();
        }
        return this.connection;
    }

    @Override
    public void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS island_generators (
                            island_id TEXT NOT NULL,
                            generator_type TEXT NOT NULL,
                            PRIMARY KEY (island_id, generator_type)
                        )
                    """);

            stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS active_generators (
                            island_id TEXT NOT NULL,
                            generator_category TEXT NOT NULL,
                            generator_type TEXT NOT NULL,
                            PRIMARY KEY (island_id, generator_category)
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Tablolar oluşturulurken hata oluştu!");
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
