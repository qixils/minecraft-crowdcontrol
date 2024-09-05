package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class EntityMixin {

	@ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public double modifyYaw(double yaw) {
		if (!(((Object) this) instanceof Player player)) return yaw;
		MovementStatusValue value = player.cc$getMovementStatus(MovementStatusType.LOOK);
		if (value == MovementStatusValue.INVERTED)
			return -yaw;
		else if (value == MovementStatusValue.DENIED)
			return 0;
		return yaw;
	}

	@ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public double modifyPitch(double pitch) {
		if (!(((Object) this) instanceof Player player)) return pitch;
		MovementStatusValue value = player.cc$getMovementStatus(MovementStatusType.LOOK);
		if (value == MovementStatusValue.INVERTED)
			return -pitch;
		else if (value == MovementStatusValue.DENIED || value == MovementStatusValue.PARTIAL)
			return 0;
		return pitch;
	}
}
