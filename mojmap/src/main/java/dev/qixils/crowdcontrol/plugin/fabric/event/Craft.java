package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

public record Craft(Player player, CraftingRecipe recipe, ItemStack result) implements Event {
}
