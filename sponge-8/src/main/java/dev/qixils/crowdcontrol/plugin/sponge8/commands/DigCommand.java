package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends ImmediateCommand {
	private final String effectName = "dig";
	private final String displayName = "Dig Hole";

	public DigCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Set<ServerLocation> locations = new HashSet<>();
		int depth = getDigDepth();
		for (Player player : players) {
			ServerLocation playerLocation = player.serverLocation();
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
			for (ServerLocation location : locations)
				location.setBlockType(BlockTypes.AIR.get());
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
