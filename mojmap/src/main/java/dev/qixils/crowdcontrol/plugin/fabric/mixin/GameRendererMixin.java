package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "setPostEffect", at = @At("HEAD"), cancellable = true)
	private void onPostEffect(Identifier effect, CallbackInfo ci) {
		if (ModdedPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "togglePostEffect", at = @At("HEAD"), cancellable = true)
	private void onTogglePostEffect(CallbackInfo ci) {
		if (ModdedPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
	private void onCheckEntityPostEffect(@Nullable Entity entity, CallbackInfo ci) {
		if (ModdedPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "clearPostEffect", at = @At("HEAD"), cancellable = true)
	private void onClearPostEffect(CallbackInfo ci) {
		if (ModdedPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	// TODO: technically missing a cancel in createReloadListener
}
