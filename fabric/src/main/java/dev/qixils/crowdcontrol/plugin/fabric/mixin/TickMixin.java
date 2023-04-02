package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class TickMixin {
	@Shadow
	private int ticks;

	@Inject(method = "tickWorlds", at = @At("HEAD"))
	public void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		new Tick(ticks).fire();
	}
}
