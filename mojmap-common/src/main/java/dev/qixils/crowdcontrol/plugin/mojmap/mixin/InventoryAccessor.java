package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import dev.qixils.crowdcontrol.plugin.mojmap.utils.ConcatenatedList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnusedMixin")
@Mixin(Inventory.class)
public interface InventoryAccessor {
	@Accessor
	List<NonNullList<ItemStack>> getCompartments();

	default List<ItemStack> viewAllItems() {
		// TODO test
		//noinspection unchecked
		return ConcatenatedList.of((Iterable<List<ItemStack>>) (Object) getCompartments());
	}
}
