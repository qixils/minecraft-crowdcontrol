package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	public void onTurn(double yaw, double pitch, CallbackInfo ci) {
		if (Components.MOVEMENT_STATUS.get(this).isProhibited(MovementStatus.Type.LOOK))
			ci.cancel();
	}
}
