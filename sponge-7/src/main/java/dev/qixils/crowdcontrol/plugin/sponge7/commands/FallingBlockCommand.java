package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {

	public FallingBlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + SpongeTextUtil.valueOf(blockType),
				Component.translatable("cc.effect.falling_block.name", Component.translatable(blockType.getTranslation().getId()))
		);
	}

	@Override
	protected Location<World> getLocation(Player player) {
		Location<World> playerLoc = player.getLocation();
		World world = playerLoc.getExtent();
		Vector3i position = new Vector3i(
				playerLoc.getX(),
				Math.min(
						playerLoc.getY() + FALLING_BLOCK_FALL_DISTANCE,
						world.getBlockMax().getY()
				),
				playerLoc.getZ()
		);
		// the below for loop does not use <= because the main execute method performs its own
		// checks
		for (int y = playerLoc.getBlockY()+1; y < position.getY(); y++) {
			BlockState block = world.getBlock(new Vector3i(position.getX(), y, position.getZ()));
			if (!BlockFinder.isPassable(block)) {
				return null;
			}
		}
		return playerLoc.setBlockPosition(position);
	}
}
