package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder.isAir;

@Getter
public class GravelCommand extends ImmediateCommand {
	private final String effectName = "gravel_hell";

	public GravelCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<ServerLocation> locations = new HashSet<>(50);
		for (ServerPlayer player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player.serverLocation())
					.locationValidator(location -> !isAir(location.blockType()) && !location.blockType().equals(BlockTypes.GRAVEL.get()))
					.shuffleLocations(false)
					.maxRadius(6)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(location -> location.setBlockType(BlockTypes.GRAVEL.get())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
