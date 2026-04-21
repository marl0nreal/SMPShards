package me.marlonreal.smpshards;

import me.marlonreal.smpshards.api.ShardsAPI;
import me.marlonreal.smpshards.api.model.PlayerData;
import me.marlonreal.smpshards.api.event.ShardGiveEvent;
import me.marlonreal.smpshards.api.event.ShardTakeEvent;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ShardsAPIImpl implements ShardsAPI {

    private final SMPShards plugin;

    public ShardsAPIImpl(@NotNull SMPShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public long getShards(@NotNull UUID uuid) {
        PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(uuid);
        if (data != null) return data.getShards();
        return plugin.getDatabaseManager().getShards(uuid);
    }

    @Override
    public long giveShards(@NotNull UUID uuid, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        ShardGiveEvent event = new ShardGiveEvent(uuid, amount, ShardGiveEvent.Source.API);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;
        applyDelta(uuid, event.getAmount());
        return event.getAmount();
    }

    @Override
    public long takeShards(@NotNull UUID uuid, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        long current = getShards(uuid);
        long actual = Math.min(amount, current);
        if (actual == 0) return 0;
        ShardTakeEvent event = new ShardTakeEvent(uuid, actual);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;
        applyDelta(uuid, -event.getAmount());
        return event.getAmount();
    }

    @Override
    public void setShards(@NotNull UUID uuid, long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        long delta = amount - getShards(uuid);
        if (delta > 0) giveShards(uuid, delta);
        else if (delta < 0) takeShards(uuid, -delta);
    }

    @Override
    public void resetShards(@NotNull UUID uuid) {
        PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(uuid);
        if (data != null) data.setShards(0);
        plugin.getDatabaseManager().resetShards(uuid);
    }

    @Override
    public boolean isInsideArea(@NotNull UUID uuid) {
        return plugin.getAFKAreaManager().isInside(uuid);
    }

    @Override
    public int getRewardCountdown(@NotNull UUID uuid) {
        return plugin.getAFKAreaManager().getRewardCountdown(uuid);
    }

    @Override
    public boolean isAreaConfigured() {
        return plugin.getAFKAreaManager().isAreaSet();
    }

    @Override
    public @NotNull List<? extends PlayerData> getTopPlayers(int limit) {
        return plugin.getDatabaseManager().getLeaderboard(Math.max(1, Math.min(limit, 100)), 0);
    }

    @Override
    public @NotNull List<? extends PlayerData> getLeaderboardPage(int page, int pageSize) {
        int size = Math.max(1, Math.min(pageSize, 100));
        return plugin.getDatabaseManager().getLeaderboard(size, Math.max(0, page) * size);
    }

    @Override
    public int getPlayerRank(@NotNull UUID uuid) {
        return plugin.getDatabaseManager().getPlayerRank(uuid);
    }

    @Override
    public int getTotalPlayerCount() {
        return plugin.getDatabaseManager().getTotalPlayerCount();
    }

    private void applyDelta(@NotNull UUID uuid, long delta) {
        if (delta == 0) return;
        PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(uuid);
        if (data != null) data.addShards(delta);
        plugin.getDatabaseManager().addShards(uuid, delta);
    }
}