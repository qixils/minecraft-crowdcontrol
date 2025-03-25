package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {
	public FallingBlockCommand(ModdedCrowdControlPlugin plugin, Block blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + BuiltInRegistries.BLOCK.getKey(blockType).getPath(),
				Component.translatable("cc.effect.falling_block.name", plugin.toAdventure(blockType.getName()))
		);
	}

	@Override
	protected Location getLocation(ServerPlayer player) {
		Location playerLoc = new Location(player);
		ServerLevel world = player.serverLevel();
		BlockPos position = BlockPos.containing(
				playerLoc.x(),
				Math.min(
						playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
						world.getMaxY() - 1
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
