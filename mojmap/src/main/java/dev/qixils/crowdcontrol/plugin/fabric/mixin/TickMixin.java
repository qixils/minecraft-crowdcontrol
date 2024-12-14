package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.MinecraftEvents;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class TickMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"), method = "runServer")
	private void beforeSetupServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STARTING.fire((MinecraftServer) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0), method = "runServer")
	private void afterSetupServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STARTED.fire((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("HEAD"), method = "stopServer")
	private void beforeShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPING.fire((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "stopServer")
	private void afterShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPED.fire((MinecraftServer) (Object) this);
	}

	@Inject(method = "tickChildren", at = @At("HEAD"))
	public void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		new Tick(((MinecraftServer) (Object) this).getTickCount()).fire();
	}
}
