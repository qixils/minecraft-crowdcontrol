package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FLOWER_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.FLOWER_MIN;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.FLOWER_RADIUS;

@Getter
public class FlowerCommand extends ImmediateCommand {
	private final String effectName = "flowers";
	private final String displayName = "Place Flowers";

	public FlowerCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location> placeLocations = new HashSet<>(FLOWER_MAX * players.size());
		for (Player player : players) {
			BlockUtil.BlockFinder finder = BlockUtil.BlockFinder.builder()
					.origin(player.getLocation())
					.maxRadius(FLOWER_RADIUS)
					.locationValidator(location ->
							!placeLocations.contains(location)
									&& location.getBlock().isReplaceable()
									&& location.clone().subtract(0, 1, 0).getBlock().getType().isSolid())
					.build();
			Location location = finder.next();
			int placed = 0;
			int toPlace = RandomUtil.nextInclusiveInt(FLOWER_MIN, FLOWER_MAX);
			while (location != null) {
				placeLocations.add(location);
				if (++placed == toPlace)
					break;
				location = finder.next();
			}
		}

		if (placeLocations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("Could not find a suitable location to place flowers");

		sync(() -> {
			for (Location location : placeLocations) {
				location.getBlock().setType(BlockUtil.FLOWERS.getRandom());
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
