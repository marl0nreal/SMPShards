package me.marlonreal.smpshards.database;

import com.zaxxer.hikari.HikariConfig;
import me.marlonreal.smpshards.SMPShards;

public class H2Backend extends AbstractSQLBackend {

    public H2Backend(SMPShards plugin) {
        super(plugin);
    }

    @Override
    protected void configureHikari(HikariConfig hc) {
        String path = plugin.getDataFolder().getAbsolutePath() + "/database";
        hc.setJdbcUrl("jdbc:h2:" + path + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=VALUE");
        hc.setDriverClassName("org.h2.Driver");
        hc.setUsername("sa");
        hc.setPassword("");
        hc.setMaximumPoolSize(4);
        hc.setPoolName("SMPShards-H2-Pool");
        hc.addDataSourceProperty("cachePrepStmts", "true");
    }

    @Override
    protected String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS `" + tableShards + "` (" +
                "`uuid`   VARCHAR(36) NOT NULL," +
                "`name`   VARCHAR(16) NOT NULL," +
                "`shards` BIGINT      NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)" +
                ")";
    }

    @Override
    protected String getUpsertSQL() {
        return "INSERT INTO `" + tableShards + "` (uuid, name, shards) VALUES (?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name)";
    }

    @Override
    protected String getBackendName() {
        return "H2";
    }
}