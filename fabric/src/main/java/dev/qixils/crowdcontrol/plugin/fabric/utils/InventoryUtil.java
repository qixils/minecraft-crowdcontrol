package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.mixin.InventoryAccessor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InventoryUtil {
	private InventoryUtil() {
	}

	public static List<ItemStack> viewAllItems(PlayerInventory inv) {
		InventoryAccessor accessor = (InventoryAccessor) inv;

		@SuppressWarnings("unchecked") // java is dumb
		Iterable<List<ItemStack>> iterable = (Iterable<List<ItemStack>>) (Object) accessor.getCompartments();

		return ConcatenatedList.of(iterable);
	}
}
