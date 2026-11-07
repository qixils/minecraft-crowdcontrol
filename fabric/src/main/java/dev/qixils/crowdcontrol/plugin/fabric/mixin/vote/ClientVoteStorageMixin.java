package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.voting.votes.ClientVote;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientVoteStorage.class)
public class ClientVoteStorageMixin {
	@Inject(method = "startVote", at = @At("RETURN"))
	public void onStartVote(UUID id, ClientVote proposal, CallbackInfo info) {
		ProposalHandler.getOptional().ifPresent(ProposalHandler::startNextProposal);
	}
}
