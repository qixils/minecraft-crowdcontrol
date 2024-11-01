package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CrowdControl;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Shadow
	private volatile boolean pause;

	@Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void injected(CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		ModdedCrowdControlPlugin plugin = ModdedCrowdControlPlugin.getInstance();
		CrowdControl cc = plugin.getCrowdControl();
		if (cc == null) return;

		plugin.setPaused(pause);
		if (pause)
			cc.pauseAll();
		else
			cc.resumeAll();
	}
}
