package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class FlingCommand extends RegionalCommandSync {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vector randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vector(vector[0], vector[1], vector[2]);
	}

	@Override
	protected @Nullable CCEffectResponse precheck(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (isActive(ccPlayer, "walk", "look"))
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
		return null;
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (player.isInsideVehicle()) return false;

		player.setVelocity(randomVector());
		return true;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while inside vehicle");
	}
}
