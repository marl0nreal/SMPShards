package me.marlonreal.smpshards.model;

import me.marlonreal.smpshards.api.model.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerDataImpl implements PlayerData {

    private final UUID uuid;
    private final String name;
    private long shards;

    public PlayerDataImpl(UUID uuid, String name, long shards) {
        this.uuid = uuid;
        this.name = name;
        this.shards = shards;
    }

    @Override
    public @NotNull UUID getUniqueId() { return uuid; }
    @Override
    public @NotNull String getName() { return name; }
    @Override
    public long getShards() { return shards; }

    public void setShards(long shards) { this.shards = shards; }
    public void addShards(long amount) { this.shards += amount; }
}
