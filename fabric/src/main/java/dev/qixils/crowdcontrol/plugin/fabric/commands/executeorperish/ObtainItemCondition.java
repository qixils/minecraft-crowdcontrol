package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Getter
public final class ObtainItemCondition implements SuccessCondition {
	private final int rewardLuck;
	private final Item item;
	private final ItemStack stack;
	private final Component component;

	public ObtainItemCondition(int rewardLuck, String key, Item item) {
		this.rewardLuck = rewardLuck;
		this.item = item;
		this.stack = new ItemStack(item);
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.obtain." + key,
				item.getName(stack).copy().withStyle(ChatFormatting.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(ServerPlayer player) {
		return player.getInventory().contains(stack);
	}
}
