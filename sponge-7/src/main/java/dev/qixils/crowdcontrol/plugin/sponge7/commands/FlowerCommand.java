package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_MAX;
import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_MIN;
import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_RADIUS;

@Getter
public class FlowerCommand extends ImmediateCommand {
	private final TypedTag<BlockType> flowers;
	private final String effectName = "flowers";
	private final String displayName = "Place Flowers";

	public FlowerCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		flowers = new TypedTag<>(CommandConstants.FLOWERS, plugin, BlockType.class);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location<World>> placeLocations = new HashSet<>(FLOWER_MAX * players.size());
		for (Player player : players) {
			BlockFinder finder = BlockFinder.builder()
					.origin(player.getLocation())
					.maxRadius(FLOWER_RADIUS)
					.locationValidator(location ->
							!placeLocations.contains(location)
									&& BlockFinder.isReplaceable(location.getBlock())
									&& BlockFinder.isSolid(location.sub(0, 1, 0).getBlock()))
					.build();
			Location<World> location = finder.next();
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
			try (StackFrame ignored = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
				for (Location<World> location : placeLocations) {
					location.setBlockType(flowers.getRandom(), BlockChangeFlags.NONE);
				}
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
