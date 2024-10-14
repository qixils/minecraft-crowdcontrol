package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LavaCommand extends ImmediateCommand {
	private final String effectName = "make_lava";

	public LavaCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<ServerLocation> locations = new HashSet<>(50);
		for (ServerPlayer player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player.serverLocation())
					.locationValidator(loc -> loc.blockType().isAnyOf(BlockTypes.WATER))
					.shuffleLocations(false)
					.maxRadius(10)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(loc -> loc.setBlockType(BlockTypes.LAVA.get())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
