package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ToastCommand extends ChatCommand {
    public ToastCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public @NotNull String getCommand() {
        return "toast";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (Player player : players) {
            Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.undiscoverRecipes(recipes);
                    player.discoverRecipes(recipes);
                }
            }.runTask(plugin);
        }
        return true;
    }
}
