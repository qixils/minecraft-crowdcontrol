package dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public class StandOnBlockCondition implements SuccessCondition {
	private final int rewardLuck;
	private final Set<BlockType> blocks;
	private final Component component;

	public StandOnBlockCondition(int rewardLuck, String key, BlockType displayItem, BlockType... otherItems) {
		this.rewardLuck = rewardLuck;
		this.blocks = new HashSet<>(otherItems.length + 1);
		this.blocks.add(displayItem);
		this.blocks.addAll(Arrays.asList(otherItems));
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.stand." + key,
				Component.translatable(displayItem.getTranslation().getId(), NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(Player player) {
		Location<World> location = player.getLocation();
		return blocks.contains(location.getBlock().getType())
				|| blocks.contains(location.sub(0, 1, 0).getBlock().getType());
	}
}
