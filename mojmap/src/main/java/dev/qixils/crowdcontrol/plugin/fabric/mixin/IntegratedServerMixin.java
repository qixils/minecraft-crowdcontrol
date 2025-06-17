package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.mojang.datafixers.DataFixer;
import dev.qixils.crowdcontrol.plugin.fabric.MinecraftEvents;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {
	public IntegratedServerMixin(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, Proxy proxy, DataFixer dataFixer, Services services) {
		super(thread, levelStorageAccess, proxy, dataFixer, services);
	}

	@Inject(at = @At("HEAD"), method = "stopServer")
	private void beforeShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPING.fire(theGame());
	}

	@Inject(at = @At("TAIL"), method = "stopServer")
	private void afterShutdownServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPED.fire(theGame());
	}
}
