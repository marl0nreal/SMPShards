package me.marlonreal.smpshards.database;

import com.zaxxer.hikari.HikariConfig;
import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.manager.ConfigManager;

public class MySQLBackend extends AbstractSQLBackend {

    public MySQLBackend(SMPShards plugin) {
        super(plugin);
    }

    @Override
    protected void configureHikari(HikariConfig hc) {
        ConfigManager cfg = plugin.getConfigManager();
        hc.setJdbcUrl("jdbc:mysql://" + cfg.getMySQLHost() + ":" + cfg.getMySQLPort()
                + "/" + cfg.getMySQLDatabase()
                + "?useSSL=" + cfg.isMySQLUseSSL()
                + "&autoReconnect=true&characterEncoding=utf8");
        hc.setUsername(cfg.getMySQLUsername());
        hc.setPassword(cfg.getMySQLPassword());
        hc.setMaximumPoolSize(cfg.getMySQLPoolSize());
        hc.setConnectionTimeout(cfg.getMySQLTimeout());
        hc.setPoolName("SMPShards-MySQL-Pool");
        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    @Override
    protected String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS `" + tableShards + "` (" +
                "`uuid`   VARCHAR(36) NOT NULL," +
                "`name`   VARCHAR(16) NOT NULL," +
                "`shards` BIGINT      NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)," +
                "INDEX `idx_shards` (`shards` DESC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }

    @Override
    protected String getCreateIndexSQL() {
        return null;
    }

    @Override
    protected String getUpsertSQL() {
        return "INSERT INTO `" + tableShards + "` (uuid, name, shards) VALUES (?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name)";
    }

    @Override
    protected String getBackendName() {
        return "MySQL";
    }
}