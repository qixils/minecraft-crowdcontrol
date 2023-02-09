package dev.qixils.crowdcontrol.plugin.paper.utils;

import org.bukkit.inventory.ItemStack;
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
		return item1.isSimilar(item2);
	}

	@Contract("null -> true; _ -> _")
	public static boolean isEmpty(@Nullable ItemStack item) {
		return item == null || item.getAmount() <= 0 || item.getType().isEmpty();
	}
}
