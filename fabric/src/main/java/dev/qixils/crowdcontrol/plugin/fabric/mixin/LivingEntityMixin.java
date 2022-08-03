package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Shadow protected boolean dead;

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource cause, final CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		if (this.dead) return;
		Death event = new Death((LivingEntity) (Object) this);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(event);
		if (event.cancelled()) ci.cancel();
	}
}
