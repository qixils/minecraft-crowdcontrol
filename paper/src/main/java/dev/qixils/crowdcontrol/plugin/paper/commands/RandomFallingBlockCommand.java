package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
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
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class RandomFallingBlockCommand extends RegionalCommandSync {
	public RandomFallingBlockCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Getter
	private final String effectName = "falling_block_random";
	private final Map<UUID, List<Material>> blocks = new HashMap<>();

	public Material getRandomBlock(World world) {
		return RandomUtil.randomElementFrom(blocks.computeIfAbsent(world.getUID(), $ -> Registry.MATERIAL.stream()
			.filter(mat -> mat.isBlock()
				&& !mat.isEmpty()
				&& mat.isSolid() // for now, try to ensure blocks are... visible
				&& mat.isOccluding() // ^^
				&& mat.asBlockType() != null
				&& world.isEnabled(mat.asBlockType())
			).toList()));
	}


	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Location playerLoc = player.getLocation();
		Location destination = playerLoc.clone();
		World world = destination.getWorld();
		destination.setY(Math.min(
			destination.getY() + FALLING_BLOCK_FALL_DISTANCE,
			world.getMaxHeight() - 1
		));
		// the below for loop does not use <= because the main execute method performs its own checks
		for (int y = playerLoc.getBlockY()+1; y < destination.getBlockY(); y++) {
			Block block = world.getBlockAt(destination.getBlockX(), y, destination.getBlockZ());
			if (!block.isPassable()) {
				return false;
			}
		}

		// get block to place
		Material block = getRandomBlock(player.getWorld());
		FallingBlock entity = world.createEntity(destination.toCenterLocation(), FallingBlock.class);
		entity.setBlockData(block.createBlockData());
		entity.setDamagePerBlock(0.75f);
		entity.setMaxDamage(4);
		entity.setDropItem(true);
		entity.setCancelDrop(false);
		world.addEntity(entity);

		return true;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Unable to find valid spawning location");
	}
}
