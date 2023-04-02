package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(RecipeBook.class)
public interface RecipeBookAccessor {
	@Accessor("recipes")
	Set<Identifier> getKnown();
}
