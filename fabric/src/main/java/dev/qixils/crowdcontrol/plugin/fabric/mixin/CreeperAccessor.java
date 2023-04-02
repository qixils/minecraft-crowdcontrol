package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreeperEntity.class)
public interface CreeperAccessor {
	@Accessor("CHARGED")
	static TrackedData<Boolean> getIsPoweredAccessor() {
		throw new AssertionError();
	}
}
