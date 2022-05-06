package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static dev.qixils.crowdcontrol.common.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {
	public FallingBlockCommand(MojmapPlugin<?> plugin, Block blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + Registry.BLOCK.getKey(blockType).getPath(),
				"Place Falling " + plugin.getTextUtil().asPlain(blockType.getName()) + " Block"
		);
	}

	@Override
	protected Location getLocation(ServerPlayer player) {
		Location playerLoc = new Location(player);
		ServerLevel world = player.getLevel();
		BlockPos position = new BlockPos(
				playerLoc.x(),
				Math.min(
						playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
						world.getMaxBuildHeight() - 1
				),
				playerLoc.z()
		);
		// the below for loop does not use <= because the main execute method performs its own
		// checks
		for (int y = (int) Math.floor(position.getY()); y < position.getY(); y++) {
			BlockState block = world.getBlockState(new BlockPos(position.getX(), y, position.getZ()));
			if (!BlockFinder.isPassable(block)) {
				return null;
			}
		}
		return playerLoc.at(position);
	}
}
