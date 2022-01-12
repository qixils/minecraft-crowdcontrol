package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

@Getter
public class FallingBlockCommand extends ImmediateCommand {
	protected final Material blockMaterial;
	private final String effectName;
	private final String displayName;

	public FallingBlockCommand(PaperCrowdControlPlugin plugin, Material blockMaterial) {
		super(plugin);
		this.blockMaterial = blockMaterial;
		this.effectName = "falling_block_" + blockMaterial.name();
		this.displayName = "Place Falling " + plugin.getTextUtil().translate(blockMaterial) + " Block";
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// TODO: update impl (GH#47)
		Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("Could not find a valid location to place block");
		for (Player player : players) {
			Location destination = player.getEyeLocation();
			destination.setY(Math.min(
					destination.getY() + FALLING_BLOCK_FALL_DISTANCE,
					player.getWorld().getMaxHeight() - 1)
			);

			Block block = destination.getBlock();
			Material type = block.getType();
			if (type.isEmpty() && type != blockMaterial) {
				resp.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> block.setType(blockMaterial, true));
			}
		}
		return resp;
	}
}
