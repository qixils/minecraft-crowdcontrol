package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends RegionalCommandSync {
	private final String effectName = "dig";

	public DigCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Streamer(s) not standing on any blocks");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		Location playerLocation = player.getLocation();
		boolean success = false;
		for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
			for (int y = getDigDepth(); y <= 0; ++y) {
				for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
					Location block = playerLocation.clone().add(x, y, z);
					if (!block.getBlock().isEmpty()) {
						block.getBlock().setType(Material.AIR);
						success = true;
					}
				}
			}
		}
		return success;
	}
}
