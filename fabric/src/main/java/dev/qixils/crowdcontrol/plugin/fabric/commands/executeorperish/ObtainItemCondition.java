package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ObtainItemCondition extends AbstractCondition {
	private final ItemStack stack;
	private final Component component;

	public ObtainItemCondition(int rewardLuck, String key, Item item) {
		this(rewardLuck, key, null, item);
	}

	public ObtainItemCondition(int rewardLuck, String key, @Nullable ConditionFlags flags, Item item) {
		super(rewardLuck, flags);
		this.stack = new ItemStack(item);
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.obtain." + key,
				item.getName(stack).copy().formatted(Formatting.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayerEntity player) {
		return player.getInventory().contains(stack);
	}
}
