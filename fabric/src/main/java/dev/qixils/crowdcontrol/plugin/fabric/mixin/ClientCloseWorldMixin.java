package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientCloseWorldMixin {
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
	private void onDisconnect(CallbackInfo info) {
		if (FabricCrowdControlPlugin.CLIENT_INITIALIZED)
			FabricPlatformClient.get().proposalHandler.reset();
	}
}
