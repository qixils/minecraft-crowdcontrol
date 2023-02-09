package dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
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
				item.asComponent().color(NamedTextColor.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(ServerPlayer player) {
		return player.inventory().contains(item);
	}
}
