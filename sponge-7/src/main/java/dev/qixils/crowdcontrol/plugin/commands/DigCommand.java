package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
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

import static dev.qixils.crowdcontrol.common.CommandConstants.DIG_RADIUS;

@Getter
public class DigCommand extends ImmediateCommand {
	private final String effectName = "dig";
	private final String displayName = "Dig Hole";

	public DigCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location<World>> locations = new HashSet<>();
		int depth = -(3 + RandomUtil.RNG.nextInt(5));
		for (Player player : players) {
			Location<World> playerLocation = player.getLocation();
			for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
				for (int y = depth; y < 0; ++y) {
					for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
						locations.add(playerLocation.add(x, y, z));
					}
				}
			}
		}

		if (locations.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("Streamer(s) not standing on any earthly blocks");

		sync(() -> {
			for (Location<World> location : locations)
				location.setBlockType(BlockTypes.AIR);
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
