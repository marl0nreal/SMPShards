package me.marlonreal.smpshards.database;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.model.PlayerDataImpl;

import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final SMPShards plugin;
    private DatabaseBackend backend;

    public DatabaseManager(SMPShards plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        String type = plugin.getConfigManager().getStorageType().trim().toLowerCase();

        switch (type) {
            case "mysql":
                backend = new MySQLBackend(plugin);
                break;
            case "h2":
                backend = new H2Backend(plugin);
                break;
            case "sqlite":
                backend = new SQLiteBackend(plugin);
                break;
            default:
                plugin.getLogger().warning(
                        "Unknown storage-type '" + type + "' in config.yml — falling back to SQLite.");
                backend = new SQLiteBackend(plugin);
                break;
        }

        return backend.connect();
    }

    public void disconnect() {
        if (backend != null) backend.disconnect();
    }

    public PlayerDataImpl loadPlayer(UUID uuid, String name) {
        return backend.loadPlayer(uuid, name);
    }

    public void savePlayer(PlayerDataImpl data) {
        backend.savePlayer(data);
    }

    public void addShards(UUID uuid, long amount) {
        backend.addShards(uuid, amount);
    }

    public long getShards(UUID uuid) {
        return backend.getShards(uuid);
    }

    public void giveShards(UUID uuid, long amount) {
        backend.addShards(uuid, amount);
    }

    public void resetShards(UUID uuid) {
        backend.resetShards(uuid);
    }

    public List<PlayerDataImpl> getLeaderboard(int limit, int offset) {
        return backend.getLeaderboard(limit, offset);
    }

    public int getTotalPlayerCount() {
        return backend.getTotalPlayerCount();
    }

    public int getPlayerRank(UUID uuid) {
        return backend.getPlayerRank(uuid);
    }
}