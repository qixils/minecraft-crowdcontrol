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

import java.util.ArrayList;
import java.util.List;

@Getter
public class GravelCommand extends Command {
    public GravelCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "gravel-hell";
    private final String displayName = "Replace Area With Gravel";

    @Override
    public Response.Result execute(Request request) {
        List<Location> locations = new ArrayList<>();
        for (Player player : CrowdControlPlugin.getPlayers())
            locations.addAll(BlockUtil.getNearbyBlocks(player.getLocation(), 6, false, BlockUtil.STONES));

        if (locations.isEmpty())
            return new Response.Result(Response.ResultType.FAILURE, "No replacable blocks nearby");

        Bukkit.getScheduler().runTask(plugin, () -> locations.forEach(location -> location.getBlock().setType(Material.GRAVEL)));
        return Response.Result.SUCCESS;
    }
}
