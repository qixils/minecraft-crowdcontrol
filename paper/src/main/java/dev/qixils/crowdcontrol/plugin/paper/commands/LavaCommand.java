package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class LavaCommand extends RegionalCommandSync {
	private final String effectName = "make_lava";

	public LavaCommand(PaperCrowdControlPlugin plugin) {
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
			.locationValidator(loc -> loc.getBlock().getType().equals(Material.WATER) || loc.getBlock().getType().equals(Material.WATER_CAULDRON))
			.shuffleLocations(false)
			.maxRadius(10)
			.build().getAll();

		if (locations.isEmpty())
			return false;

		for (Location location : locations) {
			Block block = location.getBlock();
			Material material = block.getType().equals(Material.WATER_CAULDRON) ? Material.LAVA_CAULDRON : Material.LAVA;
			block.setType(material);
		}

		return true;
	}
}
