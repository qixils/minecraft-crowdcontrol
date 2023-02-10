package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "loadEffect", at = @At("HEAD"), cancellable = true)
	private void onLoadEffect(ResourceLocation effect, CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "togglePostEffect", at = @At("HEAD"), cancellable = true)
	private void onTogglePostEffect(CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
	private void onCheckEntityPostEffect(@Nullable Entity entity, CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}

	@Inject(method = "cycleEffect", at = @At("HEAD"), cancellable = true)
	private void onCycleEffect(CallbackInfo ci) {
		if (FabricPlatformClient.SHADER_ACTIVE)
			ci.cancel();
	}
}
