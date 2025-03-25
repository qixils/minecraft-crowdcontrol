package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
	@Shadow(aliases = {"field_3902"})
	@Final
	private Options options;

	@Unique
	private boolean cc$handleIsDown(@NotNull KeyMapping key, @Nullable KeyMapping inverse, @NotNull MovementStatusType type) {
		if (!ModdedCrowdControlPlugin.CLIENT_INITIALIZED)
			return key.isDown();
		Optional<LocalPlayer> player = ModdedPlatformClient.get().player();
		if (player.isEmpty())
			return key.isDown();
		MovementStatusValue status = player.get().cc$getMovementStatus(type);
		if (status == MovementStatusValue.DENIED)
			return false;
		if (status == MovementStatusValue.INVERTED && inverse != null)
			return inverse.isDown();
		return key.isDown();
	}

	@Unique
	private boolean cc$handleIsDown(@NotNull KeyMapping key, @Nullable KeyMapping inverse) {
		return cc$handleIsDown(key, inverse, MovementStatusType.WALK);
	}

	@SuppressWarnings("SameParameterValue")
	@Unique
	private boolean cc$handleIsDown(@NotNull KeyMapping key, @NotNull MovementStatusType type) {
		return cc$handleIsDown(key, null, type);
	}

	@Unique
	private boolean cc$handleIsDown(@NotNull KeyMapping key) {
		return cc$handleIsDown(key, (KeyMapping) null);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
	public boolean isForwardDown(KeyMapping keyMapping) {
		return cc$handleIsDown(keyMapping, options.keyDown);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
	public boolean isBackDown(KeyMapping keyMapping) {
		return cc$handleIsDown(keyMapping, options.keyUp);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public boolean isLeftDown(KeyMapping keyMapping) {
		return cc$handleIsDown(keyMapping, options.keyRight);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 3))
	public boolean isRightDown(KeyMapping keyMapping) {
		return cc$handleIsDown(keyMapping, options.keyLeft);
	}

	// commented out because it interferes with non-jump actions like swimming
//	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
//	public boolean isJumpDown(KeyMapping keyMapping) {
//		return cc$handleIsDown(keyMapping, MovementStatus.Type.JUMP);
//	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 5))
	public boolean isSprintDown(KeyMapping keyMapping) {
		return cc$handleIsDown(keyMapping);
	}
}
