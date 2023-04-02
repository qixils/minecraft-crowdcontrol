package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.event.Craft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public abstract class ResultSlotMixin extends Slot {

	@Shadow
	@Final
	private CraftingInventory input;

	@Shadow
	@Final
	private PlayerEntity player;

	// dummy constructor
	public ResultSlotMixin(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Inject(method = "onTakeItem", at = @At("HEAD"))
	public void onTake(PlayerEntity player, ItemStack result, CallbackInfo ci) {
		if (player.world.isClient)
			return;
		CraftingRecipe recipe = player.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, player.world).orElse(null);
		new Craft(player, recipe, result).fire();
	}

	@Inject(method = "onCrafted(Lnet/minecraft/item/ItemStack;I)V", at = @At("HEAD"))
	public void onQuickCraft(ItemStack result, int i, CallbackInfo ci) {
		if (player.world.isClient)
			return;
		CraftingRecipe recipe = player.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, player.world).orElse(null);
		new Craft(player, recipe, result).fire();
	}

	// onSwapCraft doesn't seem necessary
}
