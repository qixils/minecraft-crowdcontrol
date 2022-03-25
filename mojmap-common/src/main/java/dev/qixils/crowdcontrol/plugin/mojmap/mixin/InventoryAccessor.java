package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings({"MixinAnnotationTarget", "UnusedMixin"})
@Mixin(Inventory.class)
public interface InventoryAccessor {
	@Accessor
	List<NonNullList<ItemStack>> compartments();
}
