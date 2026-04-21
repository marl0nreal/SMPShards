package me.marlonreal.smpshards.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public final class SchedulerUtil {

    private static final boolean FOLIA = isFolia();

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            plugin.getServer().getAsyncScheduler()
                    .runNow(plugin, $ -> task.run());
        } else {
            plugin.getServer().getScheduler()
                    .runTaskAsynchronously(plugin, task);
        }
    }

    public static void runGlobal(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            plugin.getServer().getGlobalRegionScheduler()
                    .run(plugin, $ -> task.run());
        } else {
            plugin.getServer().getScheduler()
                    .runTask(plugin, task);
        }
    }

    public static TaskHandle repeatAsync(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            long delayMs = ticksToMs(delayTicks);
            long periodMs = ticksToMs(periodTicks);
            ScheduledTask handle = plugin.getServer().getAsyncScheduler()
                    .runAtFixedRate(plugin, $ -> task.run(),
                            delayMs, periodMs, TimeUnit.MILLISECONDS);
            return handle::cancel;
        } else {
            BukkitTask task_ = plugin.getServer().getScheduler()
                    .runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
            return task_::cancel;
        }
    }

    public static TaskHandle repeatGlobal(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            long delayMs = ticksToMs(delayTicks);
            long periodMs = ticksToMs(periodTicks);
            ScheduledTask handle = plugin.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, $ -> task.run(),
                            Math.max(1, delayMs / 50), periodMs / 50);
            return handle::cancel;
        } else {
            BukkitTask task_ = plugin.getServer().getScheduler()
                    .runTaskTimer(plugin, task, delayTicks, periodTicks);
            return task_::cancel;
        }
    }

    public static void runForEntity(JavaPlugin plugin, Entity entity, Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, $ -> task.run(), null);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public static void runLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            plugin.getServer().getGlobalRegionScheduler()
                    .runDelayed(plugin, $ -> task.run(), Math.max(1, delayTicks));
        } else {
            plugin.getServer().getScheduler()
                    .runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runLaterForEntity(JavaPlugin plugin, Entity entity,
                                         Runnable task, long delayTicks) {
        if (FOLIA) {
            entity.getScheduler().runDelayed(plugin, $ -> task.run(), null, Math.max(1, delayTicks));
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    private static long ticksToMs(long ticks) {
        return ticks * 50L; // 1 tick = 50 ms
    }

    @FunctionalInterface
    public interface TaskHandle {
        void cancel();
    }
}