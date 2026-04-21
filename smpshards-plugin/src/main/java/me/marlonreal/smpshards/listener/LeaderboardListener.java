package me.marlonreal.smpshards.listener;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.gui.LeaderboardGUI;
import me.marlonreal.smpshards.manager.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LeaderboardListener implements Listener {

    private final SMPShards plugin;

    public LeaderboardListener(SMPShards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof LeaderboardGUI gui)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        int slot = event.getRawSlot();
        ConfigManager cfg = plugin.getConfigManager();

        if (slot == cfg.getCloseSlot()) {
            player.closeInventory();
        } else if (slot == cfg.getPrevPageSlot() && gui.getPage() > 0) {
            new LeaderboardGUI(plugin).open(player, gui.getPage() - 1);
        } else if (slot == cfg.getNextPageSlot() && gui.getPage() < gui.getMaxPage()) {
            new LeaderboardGUI(plugin).open(player, gui.getPage() + 1);
        }
    }
}