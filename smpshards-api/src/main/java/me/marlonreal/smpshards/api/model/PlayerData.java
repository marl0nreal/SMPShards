package me.marlonreal.smpshards.api.model;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public interface PlayerData {

    @NotNull UUID getUniqueId();
    @NotNull String getName();
    long getShards();

}