package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Craft;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@EventListener
public final class CraftItemCondition extends AbstractBooleanListeningCondition {
	private final Item item;
	private final Component component;

	public CraftItemCondition(int rewardLuck, String key, Item item) {
		this(rewardLuck, key, item, null);
	}

	public CraftItemCondition(int rewardLuck, String key, Item item, @Nullable ConditionFlags flags) {
		super(rewardLuck, flags);
		this.item = item;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.craft." + key,
				Component.translatable(new ItemStack(item).getTranslationKey(), NamedTextColor.GREEN)
		);
	}

	@Listener
	public void onCraft(Craft event) {
		UUID uuid = event.player().getUuid();
		if (!statuses.containsKey(uuid))
			return;
		if (event.result().getItem() != item)
			return;
		statuses.put(uuid, true);
	}
}
