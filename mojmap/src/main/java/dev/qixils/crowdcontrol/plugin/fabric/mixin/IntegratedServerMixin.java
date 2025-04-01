package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.MinecraftEvents;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
	@Inject(at = @At("HEAD"), method = "stopServer")
	private void beforeShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPING.fire((IntegratedServer) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "stopServer")
	private void afterShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPED.fire((IntegratedServer) (Object) this);
	}
}
