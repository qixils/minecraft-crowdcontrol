package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.qixils.crowdcontrol.plugin.fabric.MinecraftEvents;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TheGame;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class TickMixin {

	// local is broken here for some reason (maybe it can't handle AFTER shift)
	@Inject(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;theGame:Lnet/minecraft/server/TheGame;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
	private void onInitGame(CallbackInfo ci, @Local(ordinal = 0) TheGame theGame) {
		MinecraftEvents.SERVER_STARTING.fire(theGame);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus(Lnet/minecraft/server/TheGame;)Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0, shift = At.Shift.AFTER), method = "innerServerLoopWowo")
	private void afterSetupServer(CallbackInfo info) {
		MinecraftEvents.SERVER_STARTED.fire(((MinecraftServer) (Object) this).theGame());
	}

	@Inject(at = @At("HEAD"), method = "stopServer")
	private void beforeShutdownServer(TheGame theGame, CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPING.fire(theGame);
	}

	@Inject(at = @At("TAIL"), method = "stopServer")
	private void afterShutdownServer(TheGame theGame, CallbackInfo info) {
		MinecraftEvents.SERVER_STOPPED.fire(theGame);
	}

	@Inject(method = "tickChildren", at = @At("HEAD"))
	public void tick(TheGame theGame, BooleanSupplier booleanSupplier, CallbackInfo ci) {
		new Tick(((MinecraftServer) (Object) this).getTickCount()).fire();
	}
}
