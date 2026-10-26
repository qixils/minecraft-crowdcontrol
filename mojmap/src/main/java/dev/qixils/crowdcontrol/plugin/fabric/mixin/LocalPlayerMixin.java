package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient.handleIsDown;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
	public boolean isSprintDown1(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, null, MovementStatusType.WALK);
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
	public boolean isSprintDown2(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, null, MovementStatusType.WALK);
	}
}
