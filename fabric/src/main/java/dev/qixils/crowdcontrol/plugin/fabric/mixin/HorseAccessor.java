package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HorseEntity.class)
public interface HorseAccessor {
	@Invoker("setHorseVariant")
	void invokeSetVariantAndMarkings(HorseColor variant, HorseMarking markings);
}
