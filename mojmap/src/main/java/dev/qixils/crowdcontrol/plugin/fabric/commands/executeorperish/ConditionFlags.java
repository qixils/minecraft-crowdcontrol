package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import lombok.Builder;
import lombok.Data;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

@Data
@Builder
public final class ConditionFlags implements Predicate<ServerPlayer> {
	public static final @NotNull ConditionFlags DEFAULT = ConditionFlags.builder().build();
	public static final @NotNull ConditionFlags OVERWORLD = ConditionFlags.builder().allowedDimensions(singletonList(Level.OVERWORLD)).build();
	public static final @NotNull ConditionFlags NETHER = ConditionFlags.builder().allowedDimensions(singletonList(Level.NETHER)).build();
	public static final @NotNull ConditionFlags THE_END = ConditionFlags.builder().allowedDimensions(singletonList(Level.END)).build();

	@Builder.Default
	private final @NotNull List<ResourceKey<Level>> allowedDimensions = Collections.emptyList();
	@Builder.Default
	private final @NotNull Map<Item, Integer> requiredItems = Collections.emptyMap();

	@Override
	public boolean test(@NotNull ServerPlayer player) {
		if (!allowedDimensions.isEmpty() && !allowedDimensions.contains(player.serverLevel().dimension()))
			return false;
		for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
			Item item = entry.getKey();
			int required = entry.getValue();
			int count = 0;
			for (ItemStack stack : InventoryUtil.viewAllItems(player.getInventory())) {
				if (stack != null && stack.getItem() == item) {
					count += stack.getCount();
					if (count >= required)
						break;
				}
			}
			if (count < required)
				return false;
		}
		return true;
	}
}
