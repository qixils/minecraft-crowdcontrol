package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientCloseWorldMixin {
	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	private void onDisconnect(Screen screen, CallbackInfo info) {
		ProposalHandler.getOptional().ifPresent(ProposalHandler::reset);
	}
}
