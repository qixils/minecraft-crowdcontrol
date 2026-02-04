package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
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
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) not standing on any blocks");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Location playerLocation = player.getLocation();
		boolean success = false;
		int depth = getDigDepth();
		for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
			for (int y = depth; y <= 0; ++y) {
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
