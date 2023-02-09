package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

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
		itemClone1.setQuantity(1);
		ItemStack itemClone2 = item2.copy();
		itemClone2.setQuantity(1);
		return itemClone1.equalTo(itemClone2);
	}

	@Contract("null -> true; _ -> _")
	public static boolean isEmpty(@Nullable ItemStack item) {
		return item == null || item.isEmpty();
	}

	@Contract("null, null -> true; _, _ -> _")
	public static boolean isSimilar(@Nullable ItemStackSnapshot item1, @Nullable ItemStackSnapshot item2) {
		if (isEmpty(item1) && isEmpty(item2))
			return true;
		if (isEmpty(item1))
			return false;
		if (isEmpty(item2))
			return false;
		ItemStack itemClone1 = item1.createStack();
		itemClone1.setQuantity(1);
		ItemStack itemClone2 = item2.createStack();
		itemClone2.setQuantity(1);
		return itemClone1.equalTo(itemClone2);
	}

	@Contract("null -> true; _ -> _")
	public static boolean isEmpty(@Nullable ItemStackSnapshot item) {
		return item == null || item.isEmpty();
	}
}
