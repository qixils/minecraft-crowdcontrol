package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class GravelCommand extends ImmediateCommand {
    public GravelCommand(BukkitCrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "gravel_hell";
    private final String displayName = "Replace Area With Gravel";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Set<Location> locations = new HashSet<>();
        for (Player player : players)
            locations.addAll(BlockUtil.BlockFinder.builder()
                    .origin(player.getLocation())
                    .locationValidator(BlockUtil.STONES_TAG::contains)
                    .shuffleLocations(false)
                    .maxRadius(6)
                    .build().getAll());

        if (locations.isEmpty())
            return request.buildResponse().type(Response.ResultType.FAILURE).message("No replaceable blocks nearby");

        Bukkit.getScheduler().runTask(plugin, () -> locations.forEach(location -> location.getBlock().setType(Material.GRAVEL)));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
