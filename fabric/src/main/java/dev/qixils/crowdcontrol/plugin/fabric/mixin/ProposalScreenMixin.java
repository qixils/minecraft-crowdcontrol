package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.class_8373;
import net.minecraft.class_8448;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_8448.class)
public class ProposalScreenMixin {
	@Shadow private class_8448.class_8452 field_44364;

	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		if (FabricCrowdControlPlugin.CLIENT_INITIALIZED && FabricPlatformClient.get().proposalHandler.isActive())
			this.field_44364.active = false; // disable the Vote button
	}

	@Inject(method = "method_50979", at = @At("RETURN"))
	private void onUpdateVoteButton(class_8373 arg, CallbackInfoReturnable<Boolean> cir) {
		if (FabricCrowdControlPlugin.CLIENT_INITIALIZED && FabricPlatformClient.get().proposalHandler.isActive())
			this.field_44364.active = false; // disable the Vote button
	}
}
