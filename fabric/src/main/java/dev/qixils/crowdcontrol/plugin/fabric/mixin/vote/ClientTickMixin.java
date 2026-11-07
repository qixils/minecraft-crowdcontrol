package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientTickMixin {
	@Shadow
	private volatile boolean pause;

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo info) {
		if (!pause)
			ProposalHandler.getOptional().ifPresent(ProposalHandler::tick);
	}
}
