package me.marlonreal.smpshards.gui;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.manager.ConfigManager;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import me.marlonreal.smpshards.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardGUI implements InventoryHolder {

    private final SMPShards plugin;
    private int page;
    private int maxPage;
    private Inventory inventory;

    public LeaderboardGUI(SMPShards plugin) {
        this.plugin = plugin;
    }

    public int getPage() { return page; }
    public int getMaxPage() { return maxPage; }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public void open(Player player, int page) {
        SchedulerUtil.runAsync(plugin, () -> {
            ConfigManager cfg = plugin.getConfigManager();
            List<Integer> slots = cfg.getEntrySlots();
            int perPage = slots.size();
            int offset = page * perPage;

            List<PlayerDataImpl> entries = plugin.getDatabaseManager().getLeaderboard(perPage, offset);
            int total = plugin.getDatabaseManager().getTotalPlayerCount();
            int maxPage = total == 0 ? 0 : (int) Math.ceil((double) total / perPage) - 1;
            this.page = Math.max(0, Math.min(page, maxPage));
            this.maxPage = maxPage;

            SchedulerUtil.runForEntity(plugin, player, () -> {
                if (!player.isOnline()) return;
                this.inventory = build(entries, total, this.page, maxPage, cfg);
                player.openInventory(this.inventory);
            });
        });
    }

    private Inventory build(List<PlayerDataImpl> entries, int total, int currentPage, int maxPage, ConfigManager cfg) {
        String title = cfg.getLbTitle()
                .replace("{page}", String.valueOf(currentPage + 1))
                .replace("{max_page}", String.valueOf(maxPage + 1));

        Inventory inv = Bukkit.createInventory(this, cfg.getLbSize(), title);

        // Filler
        if (cfg.isFillerEnabled()) {
            ItemStack filler = makeItem(cfg.getFillerMaterial(),
                    plugin.getConfig().getString("leaderboard.filler.name", " "));
            for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
        }

        // Border (top + bottom row)
        ItemStack border = makeItem(cfg.getBorderMaterial(), " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = inv.getSize() - 9; i < inv.getSize(); i++) inv.setItem(i, border);

        // Player entries
        List<Integer> slots = cfg.getEntrySlots();
        int offset = currentPage * slots.size();
        for (int i = 0; i < slots.size() && i < entries.size(); i++) {
            inv.setItem(slots.get(i), makePlayerHead(entries.get(i), offset + i + 1, cfg));
        }

        // Info item
        ItemStack info = makeItem(
                plugin.getConfig().getString("leaderboard.info.material", "NETHER_STAR"),
                plugin.getConfig().getString("leaderboard.info.name", "&dLeaderboard"));
        ItemMeta infoMeta = info.getItemMeta();
        List<String> lore = new ArrayList<>(plugin.getConfig().getStringList("leaderboard.info.lore"));
        lore.replaceAll(s -> cfg.colorize(s)
                .replace("{page}", String.valueOf(currentPage + 1))
                .replace("{max_page}", String.valueOf(maxPage + 1))
                .replace("{total}", String.valueOf(total)));
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        inv.setItem(cfg.getInfoSlot(), info);

        // Prev / Next
        if (currentPage > 0)
            inv.setItem(cfg.getPrevPageSlot(), makeItem(
                    plugin.getConfig().getString("leaderboard.prev-page.material", "ARROW"),
                    plugin.getConfig().getString("leaderboard.prev-page.name", "&7◀ Previous Page")));
        if (currentPage < maxPage)
            inv.setItem(cfg.getNextPageSlot(), makeItem(
                    plugin.getConfig().getString("leaderboard.next-page.material", "ARROW"),
                    plugin.getConfig().getString("leaderboard.next-page.name", "&7Next Page ▶")));

        // Close
        inv.setItem(cfg.getCloseSlot(), makeItem(
                plugin.getConfig().getString("leaderboard.close.material", "BARRIER"),
                plugin.getConfig().getString("leaderboard.close.name", "&cClose")));

        return inv;
    }

    private ItemStack makePlayerHead(PlayerDataImpl pd, int rank, ConfigManager cfg) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(pd.getUniqueId()));

        meta.setDisplayName(cfg.getEntryName()
                .replace("{rank}", cfg.getRankColor(rank) + rank)
                .replace("{player}", pd.getName())
                .replace("{shards}", String.valueOf(pd.getShards())));

        List<String> lore = new ArrayList<>(cfg.getEntryLore());
        lore.replaceAll(s -> s
                .replace("{rank}", String.valueOf(rank))
                .replace("{player}", pd.getName())
                .replace("{shards}", String.valueOf(pd.getShards())));
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack makeItem(String materialName, String displayName) {
        Material mat;
        try { mat = Material.valueOf(materialName.toUpperCase()); }
        catch (IllegalArgumentException e) { mat = Material.STONE; }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().colorize(displayName));
        item.setItemMeta(meta);
        return item;
    }
}