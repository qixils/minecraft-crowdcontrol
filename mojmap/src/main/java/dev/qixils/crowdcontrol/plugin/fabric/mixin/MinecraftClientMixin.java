package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.mojang.blaze3d.platform.WindowEventHandler;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CrowdControl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
	public MinecraftClientMixin() {
		super("Client");
	}

	@Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void injected(CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		ModdedCrowdControlPlugin plugin = ModdedCrowdControlPlugin.getInstance();
		CrowdControl cc = plugin.getCrowdControl();
		if (cc == null) return;

		// no shadow due to volatility
		boolean paused = ((Minecraft) (Object) this).isPaused();
		plugin.setPaused(paused);
		if (paused) cc.pauseAll();
		else cc.resumeAll();
	}
}
