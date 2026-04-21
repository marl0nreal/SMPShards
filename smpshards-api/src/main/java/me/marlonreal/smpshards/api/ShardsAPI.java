package me.marlonreal.smpshards.api;

import me.marlonreal.smpshards.api.model.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ShardsAPI {

    long getShards(@NotNull UUID uuid);
    int getPlayerRank(@NotNull UUID uuid);
    int getTotalPlayerCount();
    boolean isInsideArea(@NotNull UUID uuid);
    int getRewardCountdown(@NotNull UUID uuid);
    boolean isAreaConfigured();

    long giveShards(@NotNull UUID uuid, long amount);
    long takeShards(@NotNull UUID uuid, long amount);
    void setShards(@NotNull UUID uuid, long amount);
    void resetShards(@NotNull UUID uuid);

    @NotNull List<? extends PlayerData> getTopPlayers(int limit);
    @NotNull List<? extends PlayerData> getLeaderboardPage(int page, int pageSize);

    static @NotNull ShardsAPI get() {
        return ShardsProvider.get();
    }

    static @Nullable ShardsAPI getOrNull() {
        return ShardsProvider.getOrNull();
    }

    static boolean isAvailable() {
        return ShardsProvider.isAvailable();
    }
}