package me.marlonreal.smpshards.manager;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import me.marlonreal.smpshards.util.SchedulerUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarManager {

    private final SMPShards plugin;
    private SchedulerUtil.TaskHandle task;

    private final Map<UUID, Integer> leaveTicks = new ConcurrentHashMap<>();

    public ActionBarManager(SMPShards plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        int ticks = plugin.getConfigManager().getActionBarTicks();
        task = SchedulerUtil.repeatAsync(plugin, this::update, ticks, ticks);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void scheduleLeaveBar(UUID uuid) {
        leaveTicks.put(uuid, 60);
    }

    private void update() {
        ConfigManager cfg = plugin.getConfigManager();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            // Leave bar
            Integer lt = leaveTicks.get(uuid);
            if (lt != null) {
                if (lt <= 0) {
                    leaveTicks.remove(uuid);
                } else {
                    sendBar(player, cfg.getActionBarLeave());
                    leaveTicks.put(uuid, lt - plugin.getConfigManager().getActionBarTicks());
                }
            }
        }
    }

    private void sendInsideBar(Player player, AFKAreaManager afk, ConfigManager cfg) {
        UUID uuid = player.getUniqueId();
        PlayerDataImpl data = afk.getPlayerData(uuid);
        long shards = data != null ? data.getShards() : 0;
        int countdown = afk.getRewardCountdown(uuid);

        String bar = cfg.getActionBarInside()
                .replace("{countdown}", String.valueOf(countdown))
                .replace("{shards}", String.valueOf(shards));

        SchedulerUtil.runForEntity(plugin, player, () -> sendBar(player, bar));
    }

    private void sendBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public void sendInsideBarNow(Player player) {
        sendInsideBar(player, plugin.getAFKAreaManager(), plugin.getConfigManager());
    }
}
