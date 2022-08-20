package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3i;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {
	public FallingBlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + blockType.key(RegistryTypes.BLOCK_TYPE).value(),
				"Place Falling " + plugin.getTextUtil().asPlain(blockType) + " Block"
		);
	}

	@Override
	protected Location<?, ?> getLocation(ServerPlayer player) {
		Location<?, ?> playerLoc = player.location();
		World<?, ?> world = playerLoc.world();
		Vector3i position = new Vector3i(
				playerLoc.x(),
				Math.min(
						playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
						world.maximumHeight() - 1
				),
				playerLoc.z()
		);
		// the below for loop does not use <= because the main execute method performs its own
		// checks
		for (int y = playerLoc.blockY(); y < position.y(); y++) {
			BlockState block = world.block(new Vector3i(position.x(), y, position.z()));
			if (!BlockFinder.isPassable(block)) {
				return null;
			}
		}
		return playerLoc.withBlockPosition(position);
	}
}
