package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	// we replace entity rendering with a little cloud of colored dots
	@Inject(method = "submitEntities", at = @At("HEAD"), cancellable = true)
	private void hideEntitiesDuringLidar(CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.CLIENT_INITIALIZED) return;
		if (!ModdedPlatformClient.get().lidar().isVisible()) return;

		ci.cancel();
	}
}
