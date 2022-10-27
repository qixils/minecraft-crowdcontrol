package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

@Getter
public final class StandOnBlockCondition extends AbstractCondition {
	private final Set<Material> blocks;
	private final Component component;

	public StandOnBlockCondition(int rewardLuck, String key, Material displayItem, Material... otherItems) {
		this(rewardLuck, key, displayItem, null, otherItems);
	}

	public StandOnBlockCondition(int rewardLuck, String key, Material displayItem, @Nullable ConditionFlags flags, Material... otherItems) {
		super(rewardLuck, flags);
		this.blocks = EnumSet.of(displayItem, otherItems);
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.stand." + key,
				Component.translatable(displayItem, NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		Location location = player.getLocation();
		return blocks.contains(location.getBlock().getType())
				|| blocks.contains(location.subtract(0, 1, 0).getBlock().getType());
	}
}
