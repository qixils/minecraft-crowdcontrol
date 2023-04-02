package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnusedMixin")
@Mixin(PlayerInventory.class)
public interface InventoryAccessor {
	@Accessor("combinedInventory")
	List<DefaultedList<ItemStack>> getCompartments();
}
