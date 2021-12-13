package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class DigCommand extends ImmediateCommand {
    private final static double RADIUS = .5D;

    public DigCommand(BukkitCrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "dig";
    private final String displayName = "Dig Hole";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Set<Block> blocks = new HashSet<>();
        int depth = -(3 + rand.nextInt(5));
        for (Player player : players) {
            for (double x = -RADIUS; x <= RADIUS; ++x) {
                for (int y = depth; y < 0; ++y) {
                    for (double z = -RADIUS; z <= RADIUS; ++z) {
                        blocks.add(player.getLocation().add(x, y, z).getBlock());
                    }
                }
            }
        }

        if (blocks.isEmpty())
            return request.buildResponse().type(Response.ResultType.RETRY).message("Streamer(s) not standing on any earthly blocks");

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Block block : blocks)
                block.setType(Material.AIR);
        });

        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
