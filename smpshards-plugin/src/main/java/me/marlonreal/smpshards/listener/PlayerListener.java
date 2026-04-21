package me.marlonreal.smpshards.listener;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.manager.AFKAreaManager;
import me.marlonreal.smpshards.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private final SMPShards plugin;

    public PlayerListener(SMPShards plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAFKAreaManager().onPlayerJoin(player);

        // Check if they spawned inside the area
        SchedulerUtil.runLaterForEntity(plugin, player, () -> {
            if (player.isOnline() && plugin.getAFKAreaManager().isAreaSet()
                    && plugin.getAFKAreaManager().isInsideArea(player.getLocation())
                    && !plugin.getAFKAreaManager().isInside(player.getUniqueId())) {
                plugin.getAFKAreaManager().handleEnter(player);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAFKAreaManager().onPlayerQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Only care about block-level changes to avoid spamming
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        AFKAreaManager afk = plugin.getAFKAreaManager();

        if (!afk.isAreaSet()) return;

        boolean wasInside = afk.isInside(player.getUniqueId());
        boolean isInside  = afk.isInsideArea(event.getTo());

        if (!wasInside && isInside) {
            afk.handleEnter(player);
        } else if (wasInside && !isInside) {
            afk.handleLeave(player);
            plugin.getActionBarManager().scheduleLeaveBar(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        AFKAreaManager afk = plugin.getAFKAreaManager();

        if (!afk.isAreaSet()) return;

        boolean wasInside = afk.isInside(player.getUniqueId());
        boolean isInside  = afk.isInsideArea(event.getTo());

        if (!wasInside && isInside) {
            afk.handleEnter(player);
        } else if (wasInside && !isInside) {
            afk.handleLeave(player);
            plugin.getActionBarManager().scheduleLeaveBar(player.getUniqueId());
        }
    }
}
