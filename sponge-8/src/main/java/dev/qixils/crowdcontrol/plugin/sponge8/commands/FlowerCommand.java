package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class FlowerCommand extends ImmediateCommand {
	private final TypedTag<BlockType> flowers;
	private final String effectName = "flowers";

	public FlowerCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
		flowers = new TypedTag<>(CommandConstants.FLOWERS, plugin, RegistryTypes.BLOCK_TYPE);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<ServerLocation> placeLocations = new HashSet<>(FLOWER_MAX * players.size());
		for (ServerPlayer player : players) {
			BlockFinder finder = BlockFinder.builder()
					.origin(player.serverLocation())
					.maxRadius(FLOWER_RADIUS)
					.locationValidator(location ->
							!placeLocations.contains(location)
									&& BlockFinder.isReplaceable(location.block())
									&& BlockFinder.isSolid(location.sub(0, 1, 0).block()))
					.build();
			ServerLocation location = finder.next();
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
			try (StackFrame ignored = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
				for (ServerLocation location : placeLocations) {
					location.setBlockType(flowers.getRandom());
				}
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
