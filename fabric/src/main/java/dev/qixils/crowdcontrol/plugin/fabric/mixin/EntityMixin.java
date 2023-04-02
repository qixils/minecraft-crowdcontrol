package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class EntityMixin {

	@ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public double modifyYaw(double yaw) {
		MovementStatus.Value value = Components.MOVEMENT_STATUS.get(this).get(MovementStatus.Type.LOOK);
		if (value == MovementStatus.Value.INVERTED)
			return -yaw;
		else if (value == MovementStatus.Value.DENIED)
			return 0;
		return yaw;
	}

	@ModifyVariable(method = "changeLookDirection", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public double modifyPitch(double pitch) {
		MovementStatus.Value value = Components.MOVEMENT_STATUS.get(this).get(MovementStatus.Type.LOOK);
		if (value == MovementStatus.Value.INVERTED)
			return -pitch;
		else if (value == MovementStatus.Value.DENIED || value == MovementStatus.Value.PARTIAL)
			return 0;
		return pitch;
	}
}
