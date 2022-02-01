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

	public ObtainItemCondition(int rewardLuck, String displayText, ItemType item) {
		this.rewardLuck = rewardLuck;
		this.item = item;
		component = Component.text("Obtain ").append(Component.text(displayText)
				.replaceText(builder -> builder.matchLiteral("%s").once()
						.replacement(item.asComponent().color(NamedTextColor.GREEN))));
	}

	@Override
	public boolean hasSucceeded(ServerPlayer player) {
		return player.inventory().contains(item);
	}
}
