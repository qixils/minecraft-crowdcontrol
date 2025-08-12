package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin {

	@WrapMethod(method = "turn")
	public void modifyYawPitch(double yaw, double pitch, Operation<Void> original) {
		if (((Object) this) instanceof Player player) {
			MovementStatusValue value = player.cc$getMovementStatus(MovementStatusType.LOOK);
			if (value == MovementStatusValue.INVERTED) {
				yaw = -yaw;
				pitch = -pitch;
			} else if (value == MovementStatusValue.DENIED) {
				yaw = 0;
				pitch = 0;
			}
		}
		original.call(yaw, pitch);
	}
}
