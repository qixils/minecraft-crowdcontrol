package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
				FabricCrowdControlPlugin.getInstance().toAdventure(item.getName(stack).copy().withStyle(ChatFormatting.GREEN))
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayer player) {
		return player.getInventory().contains(stack);
	}
}
