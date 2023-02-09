package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public final class CraftItemCondition extends AbstractBooleanListeningCondition {
	private final Material item;
	private final Component component;

	public CraftItemCondition(int rewardLuck, String key, Material item) {
		this(rewardLuck, key, item, null);
	}

	public CraftItemCondition(int rewardLuck, String key, Material item, @Nullable ConditionFlags flags) {
		super(rewardLuck, flags);
		this.item = item;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.craft." + key,
				Component.translatable(new ItemStack(item), NamedTextColor.GREEN)
		);
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		UUID uuid = event.getWhoClicked().getUniqueId();
		if (!statuses.containsKey(uuid))
			return;
		if (event.getRecipe().getResult().getType() != item)
			return;
		statuses.put(uuid, true);
	}
}
