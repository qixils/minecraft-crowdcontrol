package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public class ItemUtil {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static boolean isSimilar(@NotNull Optional<ItemStack> item1, @NotNull Optional<ItemStack> item2) {
		return isSimilar(item1.orElse(null), item2.orElse(null));
	}

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

	public static boolean isEmpty(@Nullable ItemStack item) {
		return item == null || item.isEmpty();
	}
}
