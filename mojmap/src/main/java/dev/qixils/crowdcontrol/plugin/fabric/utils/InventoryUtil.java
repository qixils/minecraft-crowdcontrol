package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtil {
	private InventoryUtil() {
	}

	public static List<ItemStack> viewAllItems(Inventory inv) {
		return ConcatenatedList.of(List.of(
			inv.getNonEquipmentItems(),
			new ArrayList<>(inv.equipment.items.values())
		));
	}
}
