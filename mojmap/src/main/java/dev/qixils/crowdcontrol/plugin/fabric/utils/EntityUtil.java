package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EntityUtil {
	private EntityUtil() {
	}

	public static void handleDie(LivingEntity entity, final DamageSource cause, final CallbackInfo ci) {
		if (entity.level().isClientSide()) return;
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		if (entity.dead) return;
		Death event = new Death(entity, cause);
		event.fire(ModdedCrowdControlPlugin.getInstance());
		if (event.cancelled()) ci.cancel();
	}

	public static void handleDamage(final Entity entity, final DamageSource cause, final float amount, final CallbackInfoReturnable<Boolean> cir) {
		if (entity.level().isClientSide()) return;
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		Damage event = new Damage(entity, cause, amount);
		event.fire(ModdedCrowdControlPlugin.getInstance());
		if (event.cancelled()) cir.setReturnValue(false);
	}
}
