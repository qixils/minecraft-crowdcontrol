package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.FLOWERS;

public class FlowerCommand extends ChatCommand {
    protected static final int RADIUS = 10;
    protected static final int MIN_RAND = 14;  // inclusive
    protected static final int MAX_RAND = 28;  // inclusive


    public FlowerCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public @NotNull String getCommand() {
        return "flowers";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (Player player : players) {
            List<Location> locations = RandomUtil.randomNearbyBlocks(player.getLocation(), RADIUS, false, BlockUtil.AIR_ARRAY);
            if (locations.isEmpty()) {continue;} // avoid scheduling a task if unnecessary
            new BukkitRunnable() {
                @Override
                public void run() {
                    int placed = 0;
                    int toPlace = MIN_RAND+rand.nextInt(MAX_RAND-MIN_RAND+1);
                    for (Location location : locations) {
                        if (location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                            ++placed;
                            location.getBlock().setType((Material) RandomUtil.randomElementFrom(FLOWERS));
                            if (placed == toPlace) {
                                break;
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
        return true;
    }
}
