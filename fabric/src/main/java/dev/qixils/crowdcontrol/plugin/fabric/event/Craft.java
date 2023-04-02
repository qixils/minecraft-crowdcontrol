package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;

public record Craft(PlayerEntity player, CraftingRecipe recipe, ItemStack result) implements Event {
}
