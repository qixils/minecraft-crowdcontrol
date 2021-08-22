package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class FallingBlockCommand extends Command {
    private static final int Y = 5;

    protected final Material blockMaterial;
    private final String effectName;
    private final String displayName;

    public FallingBlockCommand(CrowdControlPlugin plugin, Material blockMaterial) {
        super(plugin);
        this.blockMaterial = blockMaterial;
        this.effectName = "falling-block-" + blockMaterial.name();
        this.displayName = "Falling " + TextUtil.translate(blockMaterial) + " Block";
    }

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Location destination = player.getEyeLocation();
            destination.setY(Math.min(destination.getY()+Y, player.getWorld().getMaxHeight()-1));
            Block block = destination.getBlock();
            if (BlockUtil.AIR_BLOCKS.contains(block.getType()))
                Bukkit.getScheduler().runTask(plugin, () -> block.setType(blockMaterial, true));
        }
        return Response.Result.SUCCESS;
    }
}
