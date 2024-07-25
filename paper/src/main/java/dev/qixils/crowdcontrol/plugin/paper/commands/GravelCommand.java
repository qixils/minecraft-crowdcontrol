package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
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
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("No replaceable blocks nearby");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
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
