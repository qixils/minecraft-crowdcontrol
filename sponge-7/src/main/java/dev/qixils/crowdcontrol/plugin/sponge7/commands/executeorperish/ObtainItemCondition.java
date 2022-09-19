package dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;

@Getter
public class ObtainItemCondition implements SuccessCondition {
	private final int rewardLuck;
	private final ItemType item;
	private final Component component;

	public ObtainItemCondition(int rewardLuck, String key, ItemType item) {
		this.rewardLuck = rewardLuck;
		this.item = item;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.obtain." + key,
				Component.translatable(item.getTranslation().getId(), NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(Player player) {
		return player.getInventory().contains(item);
	}
}
