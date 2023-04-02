package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "loadPostProcessor", at = @At("HEAD"), cancellable = true)
	private void onLoadEffect(Identifier effect, CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "togglePostProcessorEnabled", at = @At("HEAD"), cancellable = true)
	private void onTogglePostEffect(CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "onCameraEntitySet", at = @At("HEAD"), cancellable = true)
	private void onCheckEntityPostEffect(@Nullable Entity entity, CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "cycleSuperSecretSetting", at = @At("HEAD"), cancellable = true)
	private void onCycleEffect(CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}
}
