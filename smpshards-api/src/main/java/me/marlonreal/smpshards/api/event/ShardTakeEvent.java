package me.marlonreal.smpshards.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ShardTakeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private long amount;
    private boolean cancelled;

    public ShardTakeEvent(@NotNull UUID uuid, long amount) {
        this.uuid = uuid;
        this.amount = amount;
    }

    @NotNull
    public UUID getPlayerUUID() { return uuid; }

    public long getAmount() { return amount; }

    public void setAmount(long amount) { this.amount = Math.max(1, amount); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { this.cancelled = c; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}