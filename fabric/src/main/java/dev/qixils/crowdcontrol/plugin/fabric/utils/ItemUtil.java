package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {
	private ItemUtil() {
		throw new IllegalStateException("Cannot instantiate utility class");
	}

	@Contract("null, null -> true; _, _ -> _")
	public static boolean isSimilar(@Nullable ItemStack item1, @Nullable ItemStack item2) {
		if (isEmpty(item1) && isEmpty(item2))
			return true;
		if (isEmpty(item1))
			return false;
		if (isEmpty(item2))
			return false;
		ItemStack itemClone1 = item1.copy();
		itemClone1.setCount(1);
		ItemStack itemClone2 = item2.copy();
		itemClone2.setCount(1);
		return ItemStack.isSameItemSameTags(itemClone1, itemClone2);
	}

	@Contract("null -> true; _ -> _")
	public static boolean isEmpty(@Nullable ItemStack item) {
		return item == null || item.isEmpty();
	}
}
