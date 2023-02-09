package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(RecipeBook.class)
public interface RecipeBookAccessor {
	@Accessor
	Set<ResourceLocation> getKnown();
}
