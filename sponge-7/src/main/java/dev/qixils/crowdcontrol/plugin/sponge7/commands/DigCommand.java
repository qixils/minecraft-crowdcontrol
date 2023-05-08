package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
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

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends ImmediateCommand {
	private final String effectName = "dig";

	public DigCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location<World>> locations = new HashSet<>();
		int depth = getDigDepth();
		for (Player player : players) {
			Location<World> playerLocation = player.getLocation();
			for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
				for (int y = depth; y <= 0; ++y) {
					for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
						Location<World> block = playerLocation.add(x, y, z);
						if (!block.getBlockType().equals(BlockTypes.AIR))
							locations.add(block);
					}
				}
			}
		}

		if (locations.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("Streamer(s) not standing on any blocks");

		sync(() -> {
			for (Location<World> location : locations)
				location.setBlockType(BlockTypes.AIR);
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
