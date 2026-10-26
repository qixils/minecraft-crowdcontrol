package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient.handleIsDown;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
	public boolean isForwardDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyDown, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
	public boolean isBackDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyUp, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public boolean isLeftDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyRight, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 3))
	public boolean isRightDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyLeft, MovementStatusType.WALK);
	}

	// commented out because it interferes with non-jump actions like swimming
//	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
//	public boolean isJumpDown(KeyMapping keyMapping, Operation<Boolean> original) {
//		return handleIsDown(original::call, keyMapping, null, MovementStatusType.JUMP);
//	}

	// sprint mixin is in LocalPlayerMixin on this version
}
