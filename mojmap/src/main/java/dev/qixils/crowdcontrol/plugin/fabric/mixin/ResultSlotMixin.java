package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.event.Craft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin extends Slot {

	// dummy constructor
	public ResultSlotMixin(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Inject(method = "onTake", at = @At("HEAD"))
	public void onTake(Player player, ItemStack result, CallbackInfo ci) {
		CraftingContainer craftSlots = ((ResultSlot) (Object) this).craftSlots;

		if (player.level().isClientSide) return;
		var server = player.level().theGame();
		if (server == null) return; // failsafe?
		RecipeHolder<CraftingRecipe> recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), player.level()).orElse(null);
		new Craft(player, recipe, result).fire();
	}

	@Inject(method = "onQuickCraft", at = @At("HEAD"))
	public void onQuickCraft(ItemStack result, int i, CallbackInfo ci) {
		CraftingContainer craftSlots = ((ResultSlot) (Object) this).craftSlots;
		Player player = ((ResultSlot) (Object) this).player;

		if (player.level().isClientSide) return;
		var server = player.level().theGame();
		if (server == null) return; // failsafe?
		RecipeHolder<CraftingRecipe> recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), player.level()).orElse(null);
		new Craft(player, recipe, result).fire();
	}

	// onSwapCraft doesn't seem necessary
}
