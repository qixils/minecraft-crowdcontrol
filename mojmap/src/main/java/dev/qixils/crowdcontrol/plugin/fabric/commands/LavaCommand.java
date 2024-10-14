package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LavaCommand extends ImmediateCommand {
	private final String effectName = "make_lava";

	public LavaCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<Location> locations = new HashSet<>();
		for (ServerPlayer player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player)
					.locationValidator(loc -> loc.block().is(Blocks.WATER) || loc.block().is(Blocks.WATER_CAULDRON))
					.shuffleLocations(false)
					.maxRadius(10)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(loc -> loc.block((loc.block().is(Blocks.WATER_CAULDRON) ? Blocks.LAVA_CAULDRON : Blocks.LAVA).defaultBlockState())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
