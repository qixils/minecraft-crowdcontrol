package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
	@Shadow
	@Final
	private GameOptions settings;

	private boolean handleIsDown(@NotNull KeyBinding key, @Nullable KeyBinding inverse, MovementStatus.@NotNull Type type) {
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED)
			return key.isPressed();
		Optional<ClientPlayerEntity> player = FabricPlatformClient.get().player();
		if (player.isEmpty())
			return key.isPressed();
		MovementStatus.Value status = Components.MOVEMENT_STATUS.get(player.get()).get(type);
		if (status == MovementStatus.Value.DENIED)
			return false;
		if (status == MovementStatus.Value.INVERTED && inverse != null)
			return inverse.isPressed();
		return key.isPressed();
	}

	private boolean handleIsDown(@NotNull KeyBinding key, @Nullable KeyBinding inverse) {
		return handleIsDown(key, inverse, MovementStatus.Type.WALK);
	}

	@SuppressWarnings("SameParameterValue")
	private boolean handleIsDown(@NotNull KeyBinding key, MovementStatus.@NotNull Type type) {
		return handleIsDown(key, null, type);
	}

	private boolean handleIsDown(@NotNull KeyBinding key) {
		return handleIsDown(key, (KeyBinding) null);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 0))
	public boolean isForwardDown(KeyBinding keyMapping) {
		return handleIsDown(keyMapping, settings.backKey);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 1))
	public boolean isBackDown(KeyBinding keyMapping) {
		return handleIsDown(keyMapping, settings.forwardKey);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 2))
	public boolean isLeftDown(KeyBinding keyMapping) {
		return handleIsDown(keyMapping, settings.rightKey);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 3))
	public boolean isRightDown(KeyBinding keyMapping) {
		return handleIsDown(keyMapping, settings.leftKey);
	}

	// commented out because it interferes with non-jump actions like swimming
//	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 4))
//	public boolean isJumpDown(KeyMapping keyMapping) {
//		return handleIsDown(keyMapping, MovementStatus.Type.JUMP);
//	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 5))
	public boolean isSprintDown(KeyBinding keyMapping) {
		return handleIsDown(keyMapping);
	}
}
