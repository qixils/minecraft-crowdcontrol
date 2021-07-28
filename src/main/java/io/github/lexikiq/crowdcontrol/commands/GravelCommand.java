package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GravelCommand extends ChatCommand {
    public GravelCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*7.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "hell";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        List<Location> locations = new ArrayList<>();
        for (Player player : players) {
            locations.addAll(BlockUtil.getNearbyBlocks(player.getLocation(), 6, false, BlockUtil.STONES));
        }
        if (locations.isEmpty()) {return false;}
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Location location : locations) {
                    location.getBlock().setType(Material.GRAVEL);
                }
            }
        }.runTask(plugin);
        return true;
    }
}
