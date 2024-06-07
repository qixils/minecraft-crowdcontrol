package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class LavaCommand extends ImmediateCommand {
	private final String effectName = "make_lava";

	public LavaCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location<World>> locations = new HashSet<>(50);
		for (Player player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player.getLocation())
					.locationValidator(loc -> loc.getBlock().getType().equals(BlockTypes.WATER))
					.shuffleLocations(false)
					.maxRadius(10)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(loc -> loc.setBlockType(BlockTypes.LAVA)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
