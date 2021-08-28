package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
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
import java.util.Set;

@Getter
public class GravelCommand extends Command {
    public GravelCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "gravel-hell";
    private final String displayName = "Replace Area With Gravel";

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        Set<Location> locations = new HashSet<>();
        for (Player player : CrowdControlPlugin.getPlayers())
            locations.addAll(BlockUtil.BlockFinder.builder()
                    .origin(player.getLocation())
                    .locationValidator(BlockUtil.STONES_TAG::matches)
                    .shuffleLocations(false)
                    .maxRadius(6)
                    .build().getAll());

        if (locations.isEmpty())
            return new Response.Result(Response.ResultType.FAILURE, "No replaceable blocks nearby");

        Bukkit.getScheduler().runTask(plugin, () -> locations.forEach(location -> location.getBlock().setType(Material.GRAVEL)));
        return Response.Result.SUCCESS;
    }
}
