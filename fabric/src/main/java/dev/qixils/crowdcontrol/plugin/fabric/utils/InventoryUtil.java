package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.mixin.InventoryAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InventoryUtil {
	private InventoryUtil() {
	}

	public static List<ItemStack> viewAllItems(Inventory inv) {
		InventoryAccessor accessor = (InventoryAccessor) inv;

		@SuppressWarnings("unchecked") // java is dumb
		Iterable<List<ItemStack>> iterable = (Iterable<List<ItemStack>>) (Object) accessor.getCompartments();

		return ConcatenatedList.of(iterable);
	}
}
