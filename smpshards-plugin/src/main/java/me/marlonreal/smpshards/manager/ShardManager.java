package me.marlonreal.smpshards.manager;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.util.SchedulerUtil;

public class ShardManager {

    private final SMPShards plugin;
    private SchedulerUtil.TaskHandle task;

    public ShardManager(SMPShards plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        // Run every 20 ticks (1 second)
        task = SchedulerUtil.repeatGlobal(plugin, () -> {
                plugin.getAFKAreaManager().tickSecond();
                }, 20L, 20L);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
