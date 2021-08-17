package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowerCommand extends ChatCommand {
    protected static final int RADIUS = 10;
    protected static final int MIN_RAND = 14;  // inclusive
    protected static final int MAX_RAND = 28;  // inclusive

    public FlowerCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "flowers";
    @Getter
    private final String displayName = "Place Flowers";

    @Override
    public Response.Result execute(Request request) {
        Set<Location> placeLocations = new HashSet<>();
        for (Player player : CrowdControlPlugin.getPlayers()) {
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

        if (placeLocations.isEmpty())
            return Response.Result.RETRY;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Location location : placeLocations)
                location.getBlock().setType(RandomUtil.randomElementFrom(BlockUtil.FLOWERS));
        });

        return Response.Result.SUCCESS;
    }
}
