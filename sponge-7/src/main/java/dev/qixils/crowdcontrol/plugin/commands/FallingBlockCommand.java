package dev.qixils.crowdcontrol.plugin.commands;

import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static dev.qixils.crowdcontrol.common.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class FallingBlockCommand extends BlockCommand {

	public FallingBlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		super(
				plugin,
				blockType,
				"falling_block_" + Sponge7TextUtil.valueOf(blockType),
				"Place Falling " + blockType.getTranslation().get() + " Block"
		);
	}

	@Override
	protected Location<World> getLocation(Player player) {
		Location<World> location = super.getLocation(player);
		Vector3i position = new Vector3i(
				location.getX(),
				Math.min(
						location.getY() + FALLING_BLOCK_FALL_DISTANCE,
						location.getExtent().getBlockMax().getY()
				),
				location.getZ()
		);
		return location.setBlockPosition(position);
	}
}
