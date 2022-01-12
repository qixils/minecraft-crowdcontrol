package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class GravelCommand extends ImmediateCommand {
	private final String effectName = "gravel_hell";
	private final String displayName = "Replace Area With Gravel";

	public GravelCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location<World>> locations = new HashSet<>(50);
		for (Player player : players)
			locations.addAll(BlockFinder.builder()
					.origin(player.getLocation())
					.locationValidator(location -> CommonTags.STONES.contains(
							Key.key(location.getBlock().getType().getId())))
					.shuffleLocations(false)
					.maxRadius(6)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(location -> location.setBlockType(BlockTypes.GRAVEL)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
