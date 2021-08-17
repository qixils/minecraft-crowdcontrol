package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FreezeCommand extends ChatCommand {
    protected static final Material SET_MATERIAL = Material.GLASS;
    protected static final double RADIUS = 1.5;

    public FreezeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "freeze";
    @Getter
    private final String displayName = "Freeze";

    @Override
    public Response.Result execute(Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location base = player.getLocation();
                for (double x = -RADIUS; x <= RADIUS; ++x) {
                    for (int y = -1; y <= 2; ++y) {
                        for (double z = -RADIUS; z <= RADIUS; ++z) {
                            Location location = base.clone().add(x, y, z);
                            Block block = location.getBlock();
                            if (BlockUtil.AIR_PLACE_SET.contains(block.getType())) {
                                location.getBlock().setType(SET_MATERIAL);
                            }
                        }
                    }
                }
            });
        }
        return Response.Result.SUCCESS;
    }
}
