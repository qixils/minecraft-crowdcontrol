package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import net.minecraft.client.gui.screens.voting.VotingScreen;
import net.minecraft.voting.votes.OptionId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VotingScreen.class)
public class VotingScreenMixin {
	@Shadow
	private VotingScreen.VoteButton voteButton;

	private void cc$disableVoteButton() {
		// chat gets to pick not the streamer
		if (voteButton != null && ProposalHandler.getOptional().filter(ProposalHandler::isActive).isPresent())
			voteButton.active = false;
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		cc$disableVoteButton();
	}

	@Inject(method = "updateVoteButton", at = @At("RETURN"))
	private void onUpdateVoteButton(OptionId option, CallbackInfoReturnable<Boolean> cir) {
		cc$disableVoteButton();
	}
}
