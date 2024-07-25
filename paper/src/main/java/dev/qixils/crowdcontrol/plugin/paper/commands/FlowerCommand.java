package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class FlowerCommand extends RegionalCommandSync {
	private final String effectName = "flowers";

	public FlowerCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Could not find a suitable location to place flowers");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		BlockUtil.BlockFinder finder = BlockUtil.BlockFinder.builder()
			.origin(player.getLocation())
			.maxRadius(FLOWER_RADIUS)
			.locationValidator(location ->
				location.getBlock().isReplaceable()
					&& location.clone().subtract(0, 1, 0).getBlock().getType().isSolid())
			.build();

		Location location = finder.next();
		int placed = 0;
		int toPlace = RandomUtil.nextInclusiveInt(FLOWER_MIN, FLOWER_MAX);
		while (location != null) {
			location.getBlock().setType(BlockUtil.FLOWERS.getRandom());
			if (++placed == toPlace)
				break;
			location = finder.next();
		}

		return placed > 0;
	}
}
