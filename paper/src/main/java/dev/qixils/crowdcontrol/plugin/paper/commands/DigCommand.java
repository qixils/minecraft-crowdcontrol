package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends ImmediateCommand {
	private final String effectName = "dig";
	private final String displayName = "Dig Hole";

	public DigCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location> blocks = new HashSet<>();
		int depth = getDigDepth();
		for (Player player : players) {
			Location playerLocation = player.getLocation();
			for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
				for (int y = depth; y < 0; ++y) {
					for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
						blocks.add(playerLocation.clone().add(x, y, z));
					}
				}
			}
		}

		if (blocks.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("Streamer(s) not standing on any earthly blocks");

		sync(() -> {
			for (Location location : blocks)
				location.getBlock().setType(Material.AIR);
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
