package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
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

import java.util.List;

@Getter
public class GravelCommand extends RegionalCommandSync {
	private final String effectName = "gravel_hell";

	public GravelCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No replaceable blocks nearby");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		List<Location> locations = BlockUtil.BlockFinder.builder()
			.origin(player.getLocation())
			.locationValidator(loc -> !loc.getBlock().isEmpty() && loc.getBlock().getType() != Material.GRAVEL)
			.shuffleLocations(false)
			.maxRadius(7)
			.build().getAll();

		if (locations.isEmpty()) return false;

		for (Location location : locations) {
			location.getBlock().setType(Material.GRAVEL);
		}

		return true;
	}
}
