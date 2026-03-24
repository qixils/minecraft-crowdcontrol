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
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class WaterCommand extends RegionalCommandSync {
	private final String effectName = "make_water";

	public WaterCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(PublicEffectPayload request, CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No replaceable blocks nearby");
	}

	@Override
	protected boolean executeRegionallySync(Player player, PublicEffectPayload request, CCPlayer ccPlayer) {
		List<Location> locations = BlockUtil.BlockFinder.builder()
			.origin(player.getLocation())
			.locationValidator(loc -> Tag.AIR.isTagged(loc.getBlock().getType()) || (loc.getBlock().getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()))
			.shuffleLocations(false)
			.maxRadius(10)
			.build().getAll();

		if (locations.isEmpty())
			return false;

		for (Location location : locations) {
			Block block = location.getBlock();
			if (block.getBlockData().clone() instanceof Waterlogged waterlogged) {
				waterlogged.setWaterlogged(true);
				block.setBlockData(waterlogged);
			} else
				block.setType(Material.WATER);
		}

		return true;
	}
}
