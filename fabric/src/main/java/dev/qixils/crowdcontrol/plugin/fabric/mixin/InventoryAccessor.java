package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.utils.ConcatenatedList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnusedMixin")
@Mixin(Inventory.class)
public abstract class InventoryAccessor {
	@Accessor
	public abstract List<NonNullList<ItemStack>> getCompartments();

	public List<ItemStack> viewAllItems() {
		// TODO test
		//noinspection unchecked
		return ConcatenatedList.of((Iterable<List<ItemStack>>) (Object) getCompartments());
	}
}
