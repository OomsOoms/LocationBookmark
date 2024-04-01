package io.github.oomsooms.locationbookmark;

import org.bukkit.plugin.java.JavaPlugin;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public final class LocationBookmark extends JavaPlugin {
    
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        try {
            // Create directory if it does not exist
            Path path = Paths.get(this.getDataFolder().getParentFile().getPath(), "LocationBookmark");
            String folderDir = path.toString();
            if (!path.toFile().exists()) {
                path.toFile().mkdirs();
            }
    
            // Get the Singleton instance of DatabaseManager
            dbManager = DatabaseManager.getInstance(folderDir, "LocationBookmark.db");

            // Create table if it does not exist
            String query = "CREATE TABLE IF NOT EXISTS bookmarks (id INT PRIMARY KEY, name TEXT, seed TEXT, dimension TEXT, userUuid VARCHAR(36), private BOOLEAN, x REAL, y REAL, z REAL)";
            dbManager.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        };

        // Register command
        this.getCommand("bookmark").setExecutor(new BookmarkCommand(dbManager));

        // Confirm plugin enabled
        getLogger().info("LocationBookmark Plugin Enabled");
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            try {
                dbManager.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        getLogger().info("LocationBookmark Plugin Disabled");
    }
}
