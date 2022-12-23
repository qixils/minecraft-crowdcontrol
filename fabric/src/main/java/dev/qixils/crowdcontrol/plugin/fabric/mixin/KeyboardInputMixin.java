package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
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
	private Options options;

	private boolean handleIsDown(@NotNull KeyMapping key, @Nullable KeyMapping inverse, MovementStatus.@NotNull Type type) {
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED)
			return key.isDown();
		Optional<LocalPlayer> player = FabricPlatformClient.get().player();
		if (player.isEmpty())
			return key.isDown();
		MovementStatus status = Components.MOVEMENT_STATUS.get(player.get());
		if (status.isProhibited(type))
			return false;
		if (status.isInverted(type) && inverse != null)
			return inverse.isDown();
		return key.isDown();
	}

	private boolean handleIsDown(@NotNull KeyMapping key, @Nullable KeyMapping inverse) {
		return handleIsDown(key, inverse, MovementStatus.Type.WALK);
	}

	private boolean handleIsDown(@NotNull KeyMapping key, MovementStatus.@NotNull Type type) {
		return handleIsDown(key, null, type);
	}

	private boolean handleIsDown(@NotNull KeyMapping key) {
		return handleIsDown(key, (KeyMapping) null);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
	public boolean isForwardDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping, options.keyDown);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
	public boolean isBackDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping, options.keyUp);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public boolean isLeftDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping, options.keyRight);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 3))
	public boolean isRightDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping, options.keyLeft);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
	public boolean isJumpDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping, MovementStatus.Type.JUMP);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 5))
	public boolean isSprintDown(KeyMapping keyMapping) {
		return handleIsDown(keyMapping);
	}
}
