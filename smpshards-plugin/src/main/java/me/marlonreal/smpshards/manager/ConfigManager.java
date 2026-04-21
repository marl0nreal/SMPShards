package me.marlonreal.smpshards.manager;

import me.marlonreal.smpshards.SMPShards;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final SMPShards plugin;
    private FileConfiguration cfg;

    public ConfigManager(SMPShards plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.cfg = plugin.getConfig();
    }

    // DATABASE
    public String getStorageType() { return cfg.getString("storage-type", "sqlite"); }
    public String getMySQLHost() { return cfg.getString("mysql.host", "localhost"); }
    public int getMySQLPort() { return cfg.getInt("mysql.port", 3306); }
    public String getMySQLDatabase() { return cfg.getString("mysql.database", "smpshards"); }
    public String getMySQLUsername() { return cfg.getString("mysql.username", "root"); }
    public String getMySQLPassword() { return cfg.getString("mysql.password", "password"); }
    public boolean isMySQLUseSSL() { return cfg.getBoolean("mysql.use-ssl", false); }
    public String getMySQLTablePrefix() { return cfg.getString("mysql.table-prefix", "shards_"); }
    public int getMySQLPoolSize() { return cfg.getInt("mysql.pool-size", 10); }
    public long getMySQLTimeout() { return cfg.getLong("mysql.connection-timeout", 30000); }

    // AREA
    public String getAreaWorld() { return cfg.getString("area.world", "world"); }

    public double getPos1X() { return cfg.getDouble("area.pos1.x"); }
    public double getPos1Y() { return cfg.getDouble("area.pos1.y"); }
    public double getPos1Z() { return cfg.getDouble("area.pos1.z"); }
    public double getPos2X() { return cfg.getDouble("area.pos2.x"); }
    public double getPos2Y() { return cfg.getDouble("area.pos2.y"); }
    public double getPos2Z() { return cfg.getDouble("area.pos2.z"); }

    public void setPos1(double x, double y, double z) {
        cfg.set("area.pos1.x", x); cfg.set("area.pos1.y", y); cfg.set("area.pos1.z", z);
        plugin.saveConfig();
    }

    public void setPos2(double x, double y, double z) {
        cfg.set("area.pos2.x", x); cfg.set("area.pos2.y", y); cfg.set("area.pos2.z", z);
        plugin.saveConfig();
    }

    // REWARDS
    public int getRewardInterval() { return cfg.getInt("rewards.interval", 60); }
    public int getRewardAmount() { return cfg.getInt("rewards.amount", 1); }
    public int getBonusMin() { return cfg.getInt("rewards.bonus-min", 0); }
    public int getBonusMax() { return cfg.getInt("rewards.bonus-max", 0); }

    // ACTIONBAR
    public String getActionBarInside() { return colorize(cfg.getString("actionbar.inside")); }
    public String getActionBarLeave() { return colorize(cfg.getString("actionbar.leave")); }
    public int getActionBarTicks() { return cfg.getInt("actionbar.update-ticks", 20); }

    // MESSAGES
    public String getPrefix() { return colorize(cfg.getString("messages.prefix", "&8[&6Shards&8] ")); }
    public String msg(String path) { return colorize(cfg.getString("messages." + path, "&cMissing: " + path)); }

    // LEADERBOARD
    public String getLbTitle() { return colorize(cfg.getString("leaderboard.title")); }
    public int getLbSize() { return cfg.getInt("leaderboard.size", 54); }
    public boolean isFillerEnabled() { return cfg.getBoolean("leaderboard.filler.enabled", true); }
    public String getFillerMaterial() { return cfg.getString("leaderboard.filler.material", "BLACK_STAINED_GLASS_PANE"); }
    public String getBorderMaterial() { return cfg.getString("leaderboard.border.material", "GRAY_STAINED_GLASS_PANE"); }
    public int getPrevPageSlot() { return cfg.getInt("leaderboard.prev-page.slot", 48); }
    public int getNextPageSlot() { return cfg.getInt("leaderboard.next-page.slot", 50); }
    public int getCloseSlot() { return cfg.getInt("leaderboard.close.slot", 49); }
    public int getInfoSlot() { return cfg.getInt("leaderboard.info.slot", 4); }
    public String getEntryName() { return colorize(cfg.getString("leaderboard.player-entry.name")); }
    public List<String> getEntryLore() {
        List<String> lore = cfg.getStringList("leaderboard.player-entry.lore");
        lore.replaceAll(this::colorize);
        return lore;
    }
    public List<Integer> getEntrySlots() {
        return cfg.getIntegerList("leaderboard.entry-slots");
    }
    public String getRankColor(int rank) {
        String color = cfg.getString("leaderboard.rank-colors." + rank, null);
        if (color == null) color = cfg.getString("leaderboard.rank-colors.default", "&e");
        return colorize(color);
    }

    // PARTICLES
    public boolean isParticleEnabled(String key) { return cfg.getBoolean("particles." + key + ".enabled", true); }
    public String getParticleType(String key) { return cfg.getString("particles." + key + ".type", "VILLAGER_HAPPY"); }
    public int getParticleCount(String key) { return cfg.getInt("particles." + key + ".count", 10); }

    // SOUNDS
    public boolean isSoundEnabled(String key) { return cfg.getBoolean("sounds." + key + ".enabled", true); }
    public String getSoundName(String key) { return cfg.getString("sounds." + key + ".sound", "UI_BUTTON_CLICK"); }
    public float getSoundVolume(String key) { return (float) cfg.getDouble("sounds." + key + ".volume", 1.0); }
    public float getSoundPitch(String key) { return (float) cfg.getDouble("sounds." + key + ".pitch", 1.0); }

    // UTILITY
    public String colorize(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }
}
