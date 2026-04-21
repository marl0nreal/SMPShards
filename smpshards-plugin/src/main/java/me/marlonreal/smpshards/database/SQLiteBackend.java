package me.marlonreal.smpshards.database;

import com.zaxxer.hikari.HikariConfig;
import me.marlonreal.smpshards.SMPShards;

public class SQLiteBackend extends AbstractSQLBackend {

    public SQLiteBackend(SMPShards plugin) {
        super(plugin);
    }

    @Override
    protected void configureHikari(HikariConfig hc) {
        plugin.getDataFolder().mkdirs();
        String path = plugin.getDataFolder().getAbsolutePath() + "/database.db";

        hc.setJdbcUrl("jdbc:sqlite:" + path);
        hc.setDriverClassName("org.sqlite.JDBC");
        hc.setMaximumPoolSize(1);
        hc.setConnectionTimeout(10_000);
        hc.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA busy_timeout=5000;");
        hc.setPoolName("SMPShards-SQLite-Pool");
    }

    @Override
    protected String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS `" + tableShards + "` (" +
                "`uuid`   VARCHAR(36) NOT NULL," +
                "`name`   VARCHAR(16) NOT NULL," +
                "`shards` INTEGER     NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)" +
                ")";
    }
    @Override
    protected String getUpsertSQL() {
        return "INSERT INTO `" + tableShards + "` (uuid, name, shards) VALUES (?, ?, 0) " +
                "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name";
    }

    @Override
    protected String getBackendName() {
        return "SQLite";
    }
}