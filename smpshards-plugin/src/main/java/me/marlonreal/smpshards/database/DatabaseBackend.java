package me.marlonreal.smpshards.database;

import me.marlonreal.smpshards.model.PlayerDataImpl;

import java.util.List;
import java.util.UUID;

public interface DatabaseBackend {

    boolean connect();

    void disconnect();

    PlayerDataImpl loadPlayer(UUID uuid, String name);
    void savePlayer(PlayerDataImpl data);

    void addShards(UUID uuid, long amount);
    long getShards(UUID uuid);
    void resetShards(UUID uuid);

    List<PlayerDataImpl> getLeaderboard(int limit, int offset);
    int getTotalPlayerCount();
    int getPlayerRank(UUID uuid);
}