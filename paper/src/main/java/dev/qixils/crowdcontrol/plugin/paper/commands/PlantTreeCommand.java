package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlantTreeCommand extends RegionalCommandSync {
	private final String effectName = "plant_tree";

	public PlantTreeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(ResultType.RETRY)
			.message("Streamer is not in a suitable place for tree planting");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		TreeType treeType = RandomUtil.randomElementFrom(TreeType.values());
		return player.getWorld().generateTree(player.getLocation(), random, treeType);
	}
}
