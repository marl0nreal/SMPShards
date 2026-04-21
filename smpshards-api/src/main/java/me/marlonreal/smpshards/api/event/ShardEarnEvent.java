package me.marlonreal.smpshards.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ShardEarnEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private long baseAmount;
    private long bonusAmount;
    private boolean cancelled;

    public ShardEarnEvent(@NotNull UUID uuid, long baseAmount, long bonusAmount) {
        this.uuid = uuid;
        this.baseAmount = baseAmount;
        this.bonusAmount = bonusAmount;
    }

    @NotNull
    public UUID getPlayerUUID() { return uuid; }

    public long getBaseAmount() { return baseAmount; }

    public void setBaseAmount(long amount) { this.baseAmount = Math.max(0, amount); }

    public long getBonusAmount() { return bonusAmount; }

    public void setBonusAmount(long amount) { this.bonusAmount = Math.max(0, amount); }

    public long getTotalAmount() { return baseAmount + bonusAmount; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { this.cancelled = c; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}