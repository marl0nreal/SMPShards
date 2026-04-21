package me.marlonreal.smpshards.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import me.marlonreal.smpshards.util.SchedulerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class AbstractSQLBackend implements DatabaseBackend {

    protected final SMPShards plugin;
    protected HikariDataSource dataSource;
    protected final String tableShards;

    protected AbstractSQLBackend(SMPShards plugin) {
        this.plugin = plugin;
        this.tableShards = plugin.getConfigManager().getMySQLTablePrefix() + "players";
    }

    protected abstract void configureHikari(HikariConfig hc);

    protected abstract String getBackendName();

    protected abstract String getCreateTableSQL();

    protected String getCreateIndexSQL() {
        return "CREATE INDEX IF NOT EXISTS `idx_shards` ON `" + tableShards + "` (`shards` DESC)";
    }

    protected abstract String getUpsertSQL();

    @Override
    public boolean connect() {
        HikariConfig hc = new HikariConfig();
        configureHikari(hc);
        try {
            dataSource = new HikariDataSource(hc);
            createTables();
            plugin.getLogger().info("Database connected! Backend: " + getBackendName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Database connection failed! (" + getBackendName() + ")", e);
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    protected void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(getCreateTableSQL());
            String indexSQL = getCreateIndexSQL();
            if (indexSQL != null && !indexSQL.isBlank()) {
                stmt.executeUpdate(indexSQL);
            }
        }
    }

    @Override
    public PlayerDataImpl loadPlayer(UUID uuid, String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpsertSQL())) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to upsert player " + name, e);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT shards FROM `" + tableShards + "` WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerDataImpl(uuid, name, rs.getLong("shards"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player " + name, e);
        }
        return new PlayerDataImpl(uuid, name, 0);
    }

    @Override
    public void savePlayer(PlayerDataImpl data) {
        SchedulerUtil.runAsync(plugin, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE `" + tableShards + "` SET shards = ? WHERE uuid = ?")) {
                ps.setLong(1, data.getShards());
                ps.setString(2, data.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save player " + data.getName(), e);
            }
        });
    }

    @Override
    public void addShards(UUID uuid, long amount) {
        SchedulerUtil.runAsync(plugin, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE `" + tableShards + "` SET shards = shards + ? WHERE uuid = ?")) {
                ps.setLong(1, amount);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "addShards failed for " + uuid, e);
            }
        });
    }

    @Override
    public long getShards(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT shards FROM `" + tableShards + "` WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("shards");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "getShards failed for " + uuid, e);
        }
        return 0;
    }

    @Override
    public void resetShards(UUID uuid) {
        SchedulerUtil.runAsync(plugin, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE `" + tableShards + "` SET shards = 0 WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "resetShards failed for " + uuid, e);
            }
        });
    }

    @Override
    public List<PlayerDataImpl> getLeaderboard(int limit, int offset) {
        List<PlayerDataImpl> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT uuid, name, shards FROM `" + tableShards + "` " +
                             "ORDER BY shards DESC LIMIT ? OFFSET ?")) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PlayerDataImpl(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getLong("shards")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "getLeaderboard failed", e);
        }
        return list;
    }

    @Override
    public int getTotalPlayerCount() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `" + tableShards + "`")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "getTotalPlayerCount failed", e);
        }
        return 0;
    }

    @Override
    public int getPlayerRank(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) + 1 AS rank FROM `" + tableShards + "` " +
                             "WHERE shards > (SELECT shards FROM `" + tableShards + "` WHERE uuid = ?)")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("rank");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "getPlayerRank failed for " + uuid, e);
        }
        return -1;
    }
}