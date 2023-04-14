package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import net.minecraft.class_8390;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_8390.class_8391.class)
public class VotingCostMixin {
	@Inject(method = "method_50605", at = @At("HEAD"), cancellable = true)
	private void onApplyCost(ServerPlayerEntity player, boolean dryRun, CallbackInfoReturnable<Boolean> cir) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		FabricCrowdControlPlugin plugin = FabricCrowdControlPlugin.getInstance();
		if (plugin.getModVersion(player).isEmpty()) return;
		if (plugin.getPlayerManager().getLinkedAccounts(player.getUuid()).isEmpty()) return;
		cir.setReturnValue(true); // pretend we fined the player (but don't actually because it doesn't make sense to in this context)
	}
}
