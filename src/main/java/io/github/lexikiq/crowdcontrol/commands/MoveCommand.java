package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoveCommand extends ChatCommand {
    protected final String name;
    protected final Vector vector;
    public MoveCommand(CrowdControlPlugin plugin, Vector displacement, String name) {
        super(plugin);
        vector = displacement;
        this.name = name;
    }

    public MoveCommand(CrowdControlPlugin plugin, int x, int y, int z, String name) {
        super(plugin);
        vector = new Vector(x, y, z);
        this.name = name;
    }

    @Override
    public int getCooldownSeconds() {
        return 60;
    }

    @Override
    public @NotNull String getCommand() {
        return name;
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    player.teleport(player.getLocation().add(vector));
                }
            }
        }.runTask(plugin);
        return true;
    }
}
