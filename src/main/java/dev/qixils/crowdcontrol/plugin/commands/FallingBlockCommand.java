package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
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

import java.util.List;

@Getter
public class FallingBlockCommand extends ImmediateCommand {
    private static final int Y = 5;

    protected final Material blockMaterial;
    private final String effectName;
    private final String displayName;

    public FallingBlockCommand(CrowdControlPlugin plugin, Material blockMaterial) {
        super(plugin);
        this.blockMaterial = blockMaterial;
        this.effectName = "falling_block_" + blockMaterial.name();
        this.displayName = "Falling " + TextUtil.translate(blockMaterial) + " Block";
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        for (Player player : players) {
            Location destination = player.getEyeLocation();
            destination.setY(Math.min(destination.getY()+Y, player.getWorld().getMaxHeight()-1));
            Block block = destination.getBlock();
            if (block.getType().isEmpty())
                Bukkit.getScheduler().runTask(plugin, () -> block.setType(blockMaterial, true));
        }
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
