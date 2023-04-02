package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import net.kyori.adventure.text.Component;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {
	public FallingBlockCommand(FabricCrowdControlPlugin plugin, Block blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + Registries.BLOCK.getId(blockType).getPath(),
				Component.translatable("cc.effect.falling_block.name", blockType.getName())
		);
	}

	@Override
	protected Location getLocation(ServerPlayerEntity player) {
		Location playerLoc = new Location(player);
		ServerWorld world = player.getWorld();
		BlockPos position = BlockPos.ofFloored(
				playerLoc.x(),
				Math.min(
						playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
						world.getTopY() - 1
				),
				playerLoc.z()
		);
		// the below for loop does not use <= because the main execute method performs its own
		// checks
		for (int y = (int) Math.floor(playerLoc.y()+1); y < position.getY(); y++) {
			BlockState block = world.getBlockState(new BlockPos(position.getX(), y, position.getZ()));
			if (!BlockFinder.isPassable(block)) {
				return null;
			}
		}
		return playerLoc.at(position);
	}
}
