package dev.qixils.crowdcontrol.plugin.commands.executeorperish;

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
	private final Set<BlockType> blocks;
	private final Component component;

	public StandOnBlockCondition(String displayText, BlockType displayItem, BlockType... otherItems) {
		this.blocks = new HashSet<>(otherItems.length + 1);
		this.blocks.add(displayItem);
		this.blocks.addAll(Arrays.asList(otherItems));

		component = Component.text("Stand on ").append(Component.text(displayText)
				.replaceText(builder -> builder.matchLiteral("%s").once()
						.replacement(Component.translatable(displayItem.getId())
								.color(NamedTextColor.GREEN)))
		);
	}

	public StandOnBlockCondition(Component display, BlockType... blocks) {
		this.blocks = new HashSet<>(Arrays.asList(blocks));
		this.component = Component.text("Stand on ").append(display);
	}

	@Override
	public boolean hasSucceeded(Player player) {
		Location<World> location = player.getLocation();
		return blocks.contains(location.getBlock().getType())
				|| blocks.contains(location.sub(0, 1, 0).getBlock().getType());
	}
}
