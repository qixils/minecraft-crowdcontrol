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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TorchCommand extends RegionalCommandSync {
	protected static final BlockFace[] BLOCK_FACES = new BlockFace[]{
			BlockFace.DOWN,
			BlockFace.EAST,
			BlockFace.WEST,
			BlockFace.SOUTH,
			BlockFace.NORTH
	};
	protected final boolean placeTorches;
	protected final String effectName;

	public TorchCommand(PaperCrowdControlPlugin plugin, boolean placeTorches) {
		super(plugin);
		this.placeTorches = placeTorches;
		this.effectName = placeTorches ? "lit" : "dim";
	}

	protected boolean isValidBlock(Location location) {
		// TODO: this should probably be an abstract class/method
		if (placeTorches)
			return location.getBlock().isReplaceable();
		else
			return BlockUtil.TORCHES.contains(location);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No available blocks to place/remove");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		List<Location> nearbyBlocks = BlockUtil.blockFinderBuilder()
			.origin(player.getLocation())
			.maxRadius(5)
			.locationValidator(this::isValidBlock)
			.shuffleLocations(false)
			.build().getAll();

		if (nearbyBlocks.isEmpty())
			return false;

		for (Location location : nearbyBlocks) {
			Block block = location.getBlock();
			if (placeTorches)
				placeTorch(location);
			else
				block.setType(Material.AIR, false);
		}

		return true;
	}

	protected void placeTorch(Location location) {
		Block block = location.getBlock();
		BlockFace placeFace = null;
		for (BlockFace blockFace : BLOCK_FACES) {
			boolean facingDown = blockFace == BlockFace.DOWN;
			Vector value = facingDown ? blockFace.getDirection() : blockFace.getOppositeFace().getDirection();
			if (!facingDown && placeFace != null) {
				continue;
			}
			Material type = location.clone().add(value).getBlock().getType();
			if (type.isSolid()) {
				placeFace = blockFace;
				if (facingDown) { // down takes priority
					break;
				}
			}
		}
		if (placeFace == null) {
			return;
		}
		boolean facingDown = placeFace == BlockFace.DOWN;
		Material placeBlock = facingDown ? Material.TORCH : Material.WALL_TORCH;
		block.setType(placeBlock);
		if (!facingDown) {
			Directional data = (Directional) block.getBlockData();
			data.setFacing(placeFace);
			block.setBlockData(data, false);
		}
	}
}
