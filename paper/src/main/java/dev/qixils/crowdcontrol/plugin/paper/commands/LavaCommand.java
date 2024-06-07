package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
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

@Getter
public class LavaCommand extends ImmediateCommand {
	private final String effectName = "make_lava";

	public LavaCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location> locations = new HashSet<>();
		for (Player player : players)
			locations.addAll(BlockUtil.BlockFinder.builder()
					.origin(player.getLocation())
					.locationValidator(loc -> loc.getBlock().getType().equals(Material.WATER) || loc.getBlock().getType().equals(Material.WATER_CAULDRON))
					.shuffleLocations(false)
					.maxRadius(10)
					.build().getAll());

		if (locations.isEmpty())
			return request.buildResponse()
					.type(Response.ResultType.RETRY)
					.message("No replaceable blocks nearby");

		sync(() -> locations.forEach(loc -> loc.getBlock().setType(loc.getBlock().getType().equals(Material.WATER_CAULDRON) ? Material.LAVA_CAULDRON : Material.LAVA)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
