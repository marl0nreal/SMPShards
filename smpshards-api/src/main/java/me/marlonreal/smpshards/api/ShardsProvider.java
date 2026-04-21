package me.marlonreal.smpshards.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShardsProvider {

    private static ShardsAPI instance;

    private ShardsProvider() {}

    public static void register(@NotNull ShardsAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }

    @NotNull
    public static ShardsAPI get() {
        if (instance == null) throw new IllegalStateException(
                "SMPShards is not enabled or has not finished loading yet.");
        return instance;
    }

    @Nullable
    public static ShardsAPI getOrNull() {
        return instance;
    }

    public static boolean isAvailable() {
        return instance != null;
    }
}