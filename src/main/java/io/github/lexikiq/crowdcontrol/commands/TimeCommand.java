package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeCommand extends ChatCommand {
    protected static final int ADD_TICKS = 400; // a minute in-game i think??
    public TimeCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 30;
    }

    @Override
    public @NotNull String getCommand() {
        return "zip";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        Set<World> worlds = new HashSet<>();
        for (Player player : players) {
            worlds.add(player.getWorld());
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                for (World world : worlds) {
                    world.setFullTime(world.getFullTime()+ADD_TICKS);
                }
            }
        }.runTask(plugin);
        return true;
    }
}
