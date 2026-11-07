package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import dev.qixils.crowdcontrol.plugin.fabric.vote.VoteNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.voting.votes.VotingMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VotingMaterial.Cost.class)
public class VotingCostMixin {
	@Inject(method = "deduct", at = @At("HEAD"), cancellable = true)
	private void onDeduct(ServerPlayer player, boolean dryRun, CallbackInfoReturnable<Boolean> cir) {
		// chat votes for free
		if (VoteNetworking.isChatVoting(player))
			cir.setReturnValue(true);
	}
}
