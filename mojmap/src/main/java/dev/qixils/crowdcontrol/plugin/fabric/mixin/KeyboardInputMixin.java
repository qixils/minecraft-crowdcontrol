package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

	@Unique
	private boolean cc$handleIsDown(@NotNull Function<KeyMapping, Boolean> getResult, @NotNull KeyMapping original, @Nullable KeyMapping inverse, @NotNull MovementStatusType type) {
		if (!ModdedCrowdControlPlugin.CLIENT_INITIALIZED)
			return getResult.apply(original);
		Optional<LocalPlayer> player = ModdedPlatformClient.get().player();
		if (player.isEmpty())
			return getResult.apply(original);
		MovementStatusValue status = player.get().cc$getMovementStatus(type);
		if (status == MovementStatusValue.DENIED)
			return false;
		if (status == MovementStatusValue.INVERTED && inverse != null)
			return getResult.apply(inverse);
		return getResult.apply(original);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
	public boolean isForwardDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return cc$handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyDown, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
	public boolean isBackDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return cc$handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyUp, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public boolean isLeftDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return cc$handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyRight, MovementStatusType.WALK);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 3))
	public boolean isRightDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return cc$handleIsDown(original::call, keyMapping, ((KeyboardInput) (Object) this).options.keyLeft, MovementStatusType.WALK);
	}

	// commented out because it interferes with non-jump actions like swimming
//	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
//	public boolean isJumpDown(KeyMapping keyMapping, Operation<Boolean> original) {
//		return cc$handleIsDown(original::call, keyMapping, null, MovementStatusType.JUMP);
//	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 5))
	public boolean isSprintDown(KeyMapping keyMapping, Operation<Boolean> original) {
		return cc$handleIsDown(original::call, keyMapping, null, MovementStatusType.WALK);
	}
}
