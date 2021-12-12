package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowerCommand extends ImmediateCommand {
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
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Set<Location> placeLocations = new HashSet<>();
        for (Player player : players) {
            BlockUtil.BlockFinder finder = BlockUtil.BlockFinder.builder()
                    .origin(player.getLocation())
                    .maxRadius(RADIUS)
                    .locationValidator(location -> location.getBlock().isReplaceable())
                    .build();
            Location location = finder.next();
            int placed = 0;
            int toPlace = MIN_RAND+rand.nextInt(MAX_RAND-MIN_RAND+1);
            while (location != null) {
                if (location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                    ++placed;
                    placeLocations.add(location);
                    if (placed == toPlace) {
                        break;
                    }
                }
                location = finder.next();
            }
        }

        if (placeLocations.isEmpty())
            return request.buildResponse().type(Response.ResultType.RETRY);

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Location location : placeLocations) {
                location.getBlock().setType(BlockUtil.FLOWERS.getRandom());
            }
        });

        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
