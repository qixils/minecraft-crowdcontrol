package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
public class DigCommand extends Command {
    private final static double RADIUS = .5D;
    public DigCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "dig";
    private final String displayName = "Dig Hole";

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        Set<Block> blocks = new HashSet<>();
        int depth = -(2 + rand.nextInt(4));
        for (Player player : CrowdControlPlugin.getPlayers()) {
            for (double x = -RADIUS; x <= RADIUS; ++x) {
                for (int y = depth; y < 0; ++y) {
                    for (double z = -RADIUS; z <= RADIUS; ++z) {
                        Block block = player.getLocation().add(x, y, z).getBlock();
                        if (BlockUtil.STONES_SET.contains(block.getType())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }

        if (blocks.isEmpty())
            return Response.Result.RETRY;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Block block : blocks)
                block.setType(Material.AIR);
        });

        return Response.Result.SUCCESS;
    }
}
