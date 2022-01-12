package dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;

@Getter
public class ObtainItemCondition implements SuccessCondition {
	private final ItemType item;
	private final Component component;

	public ObtainItemCondition(String displayText, ItemType item) {
		this.item = item;
		component = Component.text("Obtain ").append(Component.text(displayText)
				.replaceText(builder -> builder.matchLiteral("%s").once()
						.replacement(Component.translatable(item.getTranslation().getId())
								.color(NamedTextColor.GREEN))));
	}

	@Override
	public boolean hasSucceeded(Player player) {
		return player.getInventory().contains(item);
	}
}
