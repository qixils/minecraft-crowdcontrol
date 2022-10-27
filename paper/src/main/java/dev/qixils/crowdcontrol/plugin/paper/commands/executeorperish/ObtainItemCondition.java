package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ObtainItemCondition extends AbstractCondition {
	private final Material item;
	private final Component component;

	public ObtainItemCondition(int rewardLuck, String key, Material item) {
		this(rewardLuck, key, item, null);
	}

	public ObtainItemCondition(int rewardLuck, String key, Material item, @Nullable ConditionFlags flags) {
		super(rewardLuck, flags);
		this.item = item;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.obtain." + key,
				Component.translatable(new ItemStack(item), NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		return player.getInventory().contains(item);
	}
}
