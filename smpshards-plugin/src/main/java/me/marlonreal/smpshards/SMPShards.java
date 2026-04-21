package me.marlonreal.smpshards;

import me.marlonreal.smpshards.api.ShardsProvider;
import me.marlonreal.smpshards.command.ShardCommand;
import me.marlonreal.smpshards.database.DatabaseManager;
import me.marlonreal.smpshards.listener.LeaderboardListener;
import me.marlonreal.smpshards.listener.PlayerListener;
import me.marlonreal.smpshards.manager.AFKAreaManager;
import me.marlonreal.smpshards.manager.ActionBarManager;
import me.marlonreal.smpshards.manager.ConfigManager;
import me.marlonreal.smpshards.manager.ShardManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SMPShards extends JavaPlugin {

    private static SMPShards instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AFKAreaManager afkAreaManager;
    private ShardManager shardManager;
    private ActionBarManager actionBarManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Init managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        afkAreaManager = new AFKAreaManager(this);
        shardManager = new ShardManager(this);
        actionBarManager = new ActionBarManager(this);

        // Connect DB
        if (!databaseManager.connect()) {
            getLogger().severe("MySQL connection failed! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaderboardListener(this), this);

        // Register command
        ShardCommand cmd = new ShardCommand(this);
        getCommand("shard").setExecutor(cmd);
        getCommand("shard").setTabCompleter(cmd);

        // Start tasks
        shardManager.startTask();
        actionBarManager.startTask();

        ShardsProvider.register(new ShardsAPIImpl(this));

        getLogger().info("SMPShards v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (actionBarManager != null) actionBarManager.stopTask();
        if (shardManager != null) shardManager.stopTask();
        if (databaseManager != null) databaseManager.disconnect();

        ShardsProvider.unregister();

        getLogger().info("SMPShards disabled!");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        afkAreaManager.reload();
        actionBarManager.stopTask();
        shardManager.stopTask();
        shardManager.startTask();
        actionBarManager.startTask();
    }

    // GETTERS
    public static SMPShards getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public AFKAreaManager getAFKAreaManager() { return afkAreaManager; }
    public ShardManager getShardManager() { return shardManager; }
    public ActionBarManager getActionBarManager() { return actionBarManager; }
}
