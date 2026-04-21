package me.marlonreal.smpshards.manager;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import me.marlonreal.smpshards.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AFKAreaManager {

    private final Map<UUID, PlayerDataImpl> playerDataMap = new ConcurrentHashMap<>();
    private final Set<UUID> insideArea = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> rewardCountdown = new ConcurrentHashMap<>();

    private final SMPShards plugin;

    // REGION
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private String worldName;
    private boolean areaSet = false;

    public AFKAreaManager(SMPShards plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        ConfigManager cfg = plugin.getConfigManager();
        worldName = cfg.getAreaWorld();
        double p1x = cfg.getPos1X(), p1y = cfg.getPos1Y(), p1z = cfg.getPos1Z();
        double p2x = cfg.getPos2X(), p2y = cfg.getPos2Y(), p2z = cfg.getPos2Z();
        minX = Math.min(p1x, p2x); maxX = Math.max(p1x, p2x);
        minY = Math.min(p1y, p2y); maxY = Math.max(p1y, p2y);
        minZ = Math.min(p1z, p2z); maxZ = Math.max(p1z, p2z);
        areaSet = worldName != null && !worldName.isEmpty() && !(p1x == p2x && p1y == p2y && p1z == p2z);
    }

    // REGION CHECK
    public boolean isInsideArea(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;
        double x = Math.floor(loc.getX());
        double y = Math.floor(loc.getY());
        double z = Math.floor(loc.getZ());
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean isAreaSet() { return areaSet; }

    // PLAYER JOIN/LEAVE
    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerDataImpl data = plugin.getDatabaseManager().loadPlayer(uuid, player.getName());
        playerDataMap.put(uuid, data);
    }

    public void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        if (insideArea.contains(uuid)) {
            handleLeave(player);
        }
        PlayerDataImpl data = playerDataMap.get(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayer(data);
        }
        playerDataMap.remove(uuid);
        insideArea.remove(uuid);
        rewardCountdown.remove(uuid);
    }

    // AREA ENTER/LEAVE
    public void handleEnter(Player player) {
        UUID uuid = player.getUniqueId();
        insideArea.add(uuid);
        rewardCountdown.put(uuid, plugin.getConfigManager().getRewardInterval());

        ConfigManager cfg = plugin.getConfigManager();
        player.sendMessage(cfg.getPrefix() + cfg.msg("entered-area"));
        playSound(player, "enter-area");
        spawnParticles(player, "enter-area");
    }

    public void handleLeave(Player player) {
        UUID uuid = player.getUniqueId();
        insideArea.remove(uuid);
        rewardCountdown.remove(uuid);

        ConfigManager cfg = plugin.getConfigManager();
        player.sendMessage(cfg.getPrefix() + cfg.msg("left-area"));
        playSound(player, "leave-area");
    }

    // CALLED EVERY SECOND
    public void tickSecond() {
        ConfigManager cfg = plugin.getConfigManager();

        for (UUID uuid : new HashSet<>(insideArea)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            int countdown = rewardCountdown.getOrDefault(uuid, cfg.getRewardInterval()) - 1;
            if (countdown <= 0) {
                giveReward(player);
                rewardCountdown.put(uuid, cfg.getRewardInterval());
            } else {
                rewardCountdown.put(uuid, countdown);
            }

            SchedulerUtil.runForEntity(plugin, player, () ->
                    plugin.getActionBarManager().sendInsideBarNow(player));
        }
    }

    private void giveReward(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        int base  = cfg.getRewardAmount();
        int bonusMin = cfg.getBonusMin();
        int bonusMax = cfg.getBonusMax();
        int bonus = (bonusMax > 0) ? bonusMin + new Random().nextInt(bonusMax - bonusMin + 1) : 0;
        int total = base + bonus;

        PlayerDataImpl data = playerDataMap.get(player.getUniqueId());
        if (data != null) data.addShards(total);
        plugin.getDatabaseManager().addShards(player.getUniqueId(), total);

        long newTotal = data != null ? data.getShards() : plugin.getDatabaseManager().getShards(player.getUniqueId());
        String msg = cfg.getPrefix() + cfg.msg("shard-received")
                .replace("{amount}", String.valueOf(base))
                .replace("{total}", String.valueOf(newTotal));
        player.sendMessage(msg);

        if (bonus > 0) {
            player.sendMessage(cfg.getPrefix() + cfg.msg("bonus-received")
                    .replace("{bonus}", String.valueOf(bonus)));
        }

        SchedulerUtil.runForEntity(plugin, player, () -> {
            playSound(player, "shard-received");
            spawnParticles(player, "shard-received");
        });
    }

    // Helpers
    private void playSound(Player player, String key) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isSoundEnabled(key)) return;
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(cfg.getSoundName(key));
            player.playSound(player.getLocation(), sound, cfg.getSoundVolume(key), cfg.getSoundPitch(key));
        } catch (IllegalArgumentException ignored) {}
    }

    private void spawnParticles(Player player, String key) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isParticleEnabled(key)) return;
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(cfg.getParticleType(key));
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0),
                    cfg.getParticleCount(key), 0.5, 0.5, 0.5, 0);
        } catch (IllegalArgumentException ignored) {}
    }

    // Accessors
    public boolean isInside(UUID uuid) { return insideArea.contains(uuid); }
    public int getRewardCountdown(UUID uuid) { return rewardCountdown.getOrDefault(uuid, plugin.getConfigManager().getRewardInterval()); }
    public PlayerDataImpl getPlayerData(UUID uuid) { return playerDataMap.get(uuid); }

    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMinZ() { return minZ; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }
    public double getMaxZ() { return maxZ; }
    public String getWorldName() { return worldName; }
}
