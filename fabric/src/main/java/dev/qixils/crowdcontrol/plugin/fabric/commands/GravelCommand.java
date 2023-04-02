package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class GravelCommand extends ImmediateCommand {
	private final String effectName = "gravel_hell";

	public GravelCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Set<Location> locations = new HashSet<>();
		for (ServerPlayerEntity player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player)
					.locationValidator(loc ->
							CommonTags.STONES_EXCEPT_GRAVEL.contains(Registries.BLOCK.getId(loc.block().getBlock()).toString()))
					.shuffleLocations(false)
					.maxRadius(6)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(location -> location.block(Blocks.GRAVEL.getDefaultState())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
