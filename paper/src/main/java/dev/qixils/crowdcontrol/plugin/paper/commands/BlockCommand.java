package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class BlockCommand extends ImmediateCommand {
	protected final Material material;
	private final String effectName;
	private final String displayName;

	public BlockCommand(PaperCrowdControlPlugin plugin, Material block) {
		this(
				plugin,
				block,
				"block_" + block.name(),
				"Place " + plugin.getTextUtil().translate(block) + " Block"
		);
	}

	protected BlockCommand(PaperCrowdControlPlugin plugin, Material block, String effectName, String displayName) {
		super(plugin);
		this.material = block;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location getLocation(Player player) {
		return player.getLocation();
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No available locations to set blocks");
		for (Player player : players) {
			if (!player.getLocation().getBlock().isReplaceable())
				continue;
			Location location = getLocation(player);
			if (location == null)
				continue;
			Block block = location.getBlock();
			if (block.isReplaceable() && block.getType() != material) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> block.setType(material));
			}
		}
		return result;
	}
}
