package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.FLOWERS;

public class FlowerCommand extends ChatCommand {
    protected static final int RADIUS = 10;
    protected static final int MIN_RAND = 14;  // inclusive
    protected static final int MAX_RAND = 28;  // inclusive

    public FlowerCommand(CrowdControlPlugin plugin) {
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
        Set<Location> placeLocations = new HashSet<>();
        for (Player player : players) {
            List<Location> locations = RandomUtil.randomNearbyBlocks(player.getLocation(), RADIUS, false, BlockUtil.AIR_PLACE);
            int placed = 0;
            int toPlace = MIN_RAND+rand.nextInt(MAX_RAND-MIN_RAND+1);
            for (Location location : locations) {
                if (location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                    ++placed;
                    placeLocations.add(location);
                    if (placed == toPlace) {
                        break;
                    }
                }
            }
        }
        if (placeLocations.isEmpty()) {return false;}
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Location location : placeLocations) {
                    location.getBlock().setType((Material) RandomUtil.randomElementFrom(FLOWERS));
                }
            }
        }.runTask(plugin);
        return true;
    }
}
