package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
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
        Response.Builder resp = request.buildResponse().type(ResultType.FAILURE).message("Could not find a valid location to place block");
        for (Player player : players) {
            Location destination = player.getEyeLocation();
            destination.setY(Math.min(destination.getY()+Y, player.getWorld().getMaxHeight()-1));

            Block block = destination.getBlock();
            Material type = block.getType();
            if (type.isEmpty() && type != blockMaterial) {
                resp.type(ResultType.SUCCESS).message("SUCCESS");
                Bukkit.getScheduler().runTask(plugin, () -> block.setType(blockMaterial, true));
            }
        }
        return resp;
    }
}
