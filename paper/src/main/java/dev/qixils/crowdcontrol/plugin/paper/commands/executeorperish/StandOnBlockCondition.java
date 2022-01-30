package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class StandOnBlockCondition implements SuccessCondition {
	private final int rewardLuck;
	private final Set<Material> blocks;
	private final Component component;

	public StandOnBlockCondition(int rewardLuck, String displayText, Material displayItem, Material... otherItems) {
		this.rewardLuck = rewardLuck;
		this.blocks = EnumSet.of(displayItem, otherItems);
		this.component = Component.text("Stand on ").append(Component.text(displayText)
				.replaceText(builder -> builder.matchLiteral("%s").once()
						.replacement(Component.translatable(displayItem).color(NamedTextColor.GREEN)))
		);
	}

	public StandOnBlockCondition(int rewardLuck, Component display, Material first, Material... other) {
		this.rewardLuck = rewardLuck;
		this.blocks = EnumSet.of(first, other);
		this.component = Component.text("Stand on ").append(display);
	}

	@Override
	public boolean hasSucceeded(Player player) {
		Location location = player.getLocation();
		return blocks.contains(location.getBlock().getType())
				|| blocks.contains(location.subtract(0, 1, 0).getBlock().getType());
	}
}
