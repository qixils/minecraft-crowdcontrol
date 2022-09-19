package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class ObtainItemCondition implements SuccessCondition {
	private final int rewardLuck;
	private final Material item;
	private final Component component;

	public ObtainItemCondition(int rewardLuck, String key, Material item) {
		this.rewardLuck = rewardLuck;
		this.item = item;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.obtain." + key,
				Component.translatable(new ItemStack(item), NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(Player player) {
		return player.getInventory().contains(item);
	}
}
