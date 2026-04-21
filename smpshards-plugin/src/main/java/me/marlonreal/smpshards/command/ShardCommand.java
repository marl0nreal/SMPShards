package me.marlonreal.smpshards.command;

import me.marlonreal.smpshards.SMPShards;
import me.marlonreal.smpshards.gui.LeaderboardGUI;
import me.marlonreal.smpshards.manager.AFKAreaManager;
import me.marlonreal.smpshards.manager.ConfigManager;
import me.marlonreal.smpshards.model.PlayerDataImpl;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShardCommand implements CommandExecutor, TabCompleter {

    private final SMPShards plugin;

    public ShardCommand(SMPShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "top", "leaderboard", "lb" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("player-only"));
                    return true;
                }
                new LeaderboardGUI(plugin).open(player, 0);
            }

            case "shards", "balance", "bal" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("player-only"));
                    return true;
                }
                PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(player.getUniqueId());
                long shards = data != null ? data.getShards() : plugin.getDatabaseManager().getShards(player.getUniqueId());
                player.sendMessage(cfg.getPrefix() + cfg.msg("your-shards")
                        .replace("{shards}", String.valueOf(shards)));
            }

            case "setpos1" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("player-only"));
                    return true;
                }
                var target = player.getTargetBlockExact(10);
                if (target == null) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-block-in-sight"));
                    return true;
                }
                double x = target.getX(), y = target.getY(), z = target.getZ();
                cfg.setPos1(x, y, z);
                plugin.getAFKAreaManager().reload();
                sender.sendMessage(cfg.getPrefix() + cfg.msg("pos1-set")
                        .replace("{x}", String.format("%.1f", x))
                        .replace("{y}", String.format("%.1f", y))
                        .replace("{z}", String.format("%.1f", z)));
            }

            case "setpos2" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("player-only"));
                    return true;
                }
                var target = player.getTargetBlockExact(10);
                if (target == null) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-block-in-sight"));
                    return true;
                }
                double x = target.getX(), y = target.getY(), z = target.getZ();
                cfg.setPos2(x, y, z);
                plugin.getAFKAreaManager().reload();
                sender.sendMessage(cfg.getPrefix() + cfg.msg("pos2-set")
                        .replace("{x}", String.format("%.1f", x))
                        .replace("{y}", String.format("%.1f", y))
                        .replace("{z}", String.format("%.1f", z)));
            }

            case "give" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("usage-give").replace("{label}", label));
                    return true;
                }
                long amount;
                try {
                    amount = Long.parseLong(args[2]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("invalid-amount"));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getDatabaseManager().giveShards(target.getUniqueId(), amount);
                PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(target.getUniqueId());
                if (data != null) data.addShards(amount);
                sender.sendMessage(cfg.getPrefix() + cfg.msg("shards-given")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{player}", args[1]));
            }

            case "reset" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("usage-reset").replace("{label}", label));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getDatabaseManager().resetShards(target.getUniqueId());
                PlayerDataImpl data = plugin.getAFKAreaManager().getPlayerData(target.getUniqueId());
                if (data != null) data.setShards(0);
                sender.sendMessage(cfg.getPrefix() + cfg.msg("shards-reset")
                        .replace("{player}", args[1]));
            }

            case "info" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                AFKAreaManager afk = plugin.getAFKAreaManager();
                sender.sendMessage(cfg.msg("info-header"));
                sender.sendMessage(cfg.msg("info-title"));
                sender.sendMessage(cfg.msg("info-world").replace("{world}", afk.getWorldName()));
                sender.sendMessage(cfg.msg("info-set").replace("{set}", String.valueOf(afk.isAreaSet())));
                sender.sendMessage(cfg.msg("info-pos1")
                        .replace("{x}", String.format("%.1f", afk.getMinX()))
                        .replace("{y}", String.format("%.1f", afk.getMinY()))
                        .replace("{z}", String.format("%.1f", afk.getMinZ())));
                sender.sendMessage(cfg.msg("info-pos2")
                        .replace("{x}", String.format("%.1f", afk.getMaxX()))
                        .replace("{y}", String.format("%.1f", afk.getMaxY()))
                        .replace("{z}", String.format("%.1f", afk.getMaxZ())));
                sender.sendMessage(cfg.msg("info-footer"));
            }

            case "reload" -> {
                if (!sender.hasPermission("smpshards.admin")) {
                    sender.sendMessage(cfg.getPrefix() + cfg.msg("no-permission"));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(plugin.getConfigManager().getPrefix() +
                        plugin.getConfigManager().msg("reload-success"));
            }

            default -> sendHelp(sender, label);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        ConfigManager cfg = plugin.getConfigManager();
        boolean admin = sender.hasPermission("smpshards.admin");

        sender.sendMessage(cfg.msg("help-header"));
        sender.sendMessage(cfg.msg("help-title").replace("{label}", label));
        sender.sendMessage(cfg.msg("help-header"));
        sender.sendMessage(cfg.msg("help-top").replace("{label}", label));
        sender.sendMessage(cfg.msg("help-shards").replace("{label}", label));

        if (admin) {
            sender.sendMessage(cfg.msg("help-admin-separator"));
            for (String key : List.of("setpos1", "setpos2", "give", "reset", "info", "reload")) {
                sender.sendMessage(cfg.msg("help-" + key).replace("{label}", label));
            }
        }

        sender.sendMessage(cfg.msg("help-footer"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>(Arrays.asList("top", "shards"));
            if (sender.hasPermission("smpshards.admin")) {
                cmds.addAll(Arrays.asList("setpos1", "setpos2", "give", "reset", "reload", "info"));
            }
            return cmds.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("reset"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}