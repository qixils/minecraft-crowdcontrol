package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWERS;
import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_MAX;
import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_MIN;
import static dev.qixils.crowdcontrol.common.CommandConstants.FLOWER_RADIUS;

@Getter
public class FlowerCommand extends ImmediateCommand {
	private final String effectName = "flowers";
	private final String displayName = "Place Flowers";
	private final MappedKeyedTag<Block> flowers = new TypedTag<>(FLOWERS, Registry.BLOCK);

	public FlowerCommand(MojmapPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Location> placeLocations = new HashSet<>(FLOWER_MAX * players.size());
		for (ServerPlayer player : players) {
			BlockFinder finder = BlockFinder.builder()
					.origin(player)
					.maxRadius(FLOWER_RADIUS)
					.locationValidator(location ->
							!placeLocations.contains(location)
									&& BlockFinder.isReplaceable(location)
									&& BlockFinder.isSolid(location.add(0, -1, 0).block()))
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
				location.block(flowers.getRandom().defaultBlockState());
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
