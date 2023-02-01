package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

@Getter
public class FallingBlockCommand extends BlockCommand {
	public FallingBlockCommand(PaperCrowdControlPlugin plugin, Material block) {
		super(
				plugin,
				block,
				"falling_block_" + block.name(),
				Component.translatable("cc.effect.falling_block.name", Component.translatable(block))
		);
	}

	@Override
	protected Location getLocation(Player player) {
		Location playerLoc = player.getLocation();
		Location destination = playerLoc.clone();
		World world = playerLoc.getWorld();
		destination.setY(Math.min(
				destination.getY() + FALLING_BLOCK_FALL_DISTANCE,
				world.getMaxHeight() - 1
		));
		// the below for loop does not use <= because the main execute method performs its own
		// checks
		for (int y = playerLoc.getBlockY()+1; y < destination.getBlockY(); y++) {
			Block block = world.getBlockAt(destination.getBlockX(), y, destination.getBlockZ());
			if (!block.isPassable()) {
				return null;
			}
		}
		return destination;
	}
}
