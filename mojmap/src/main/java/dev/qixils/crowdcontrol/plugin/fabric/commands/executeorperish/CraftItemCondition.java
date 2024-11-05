package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Craft;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.item.Item;
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
				ModdedCrowdControlPlugin.getInstance().adventure().asAdventure(item.getName()).color(NamedTextColor.GREEN)
		);
	}

	@Listener
	public void onCraft(Craft event) {
		UUID uuid = event.player().getUUID();
		if (!statuses.containsKey(uuid))
			return;
		if (event.result().getItem() != item)
			return;
		statuses.put(uuid, true);
	}
}
