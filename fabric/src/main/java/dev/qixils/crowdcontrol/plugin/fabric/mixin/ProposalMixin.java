package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.class_8367;
import net.minecraft.class_8471;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(class_8471.class)
public class ProposalMixin {
	@Inject(method = "method_51071", at = @At("TAIL"))
	public void onRegisterProposal(UUID id, class_8367 proposal, CallbackInfo info) {
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED) return;
		FabricPlatformClient.get().proposalHandler.startNextProposal();
	}
}
