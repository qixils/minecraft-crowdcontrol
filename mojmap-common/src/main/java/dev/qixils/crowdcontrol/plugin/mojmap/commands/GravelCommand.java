package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class GravelCommand extends ImmediateCommand {
	private final String effectName = "gravel_hell";
	private final String displayName = "Replace Area With Gravel";

	public GravelCommand(MojmapPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Location> locations = new HashSet<>();
		for (ServerPlayer player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player)
					.locationValidator(loc ->
							CommonTags.STONES_EXCEPT_GRAVEL.contains(Registry.BLOCK.getKey(loc.block().getBlock()).toString()))
					.shuffleLocations(false)
					.maxRadius(6)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(location -> location.block(Blocks.GRAVEL.defaultBlockState())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
