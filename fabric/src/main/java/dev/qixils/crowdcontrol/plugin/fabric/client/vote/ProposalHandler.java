package dev.qixils.crowdcontrol.plugin.fabric.client.vote;

import net.minecraft.client.Minecraft;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalVote.DURATION_PADDING;
import static dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalVote.MIN_DURATION;

/** Handles Twitch-based chat polling */
public final class ProposalHandler {
	private static @Nullable ProposalHandler INSTANCE;
	public final ProposalHud overlay = new ProposalHud(this);
	private final TwitchChat twitchChat;
	private final Minecraft client;
	private @Nullable ProposalVote currentProposal = null;
	private int proposalCount = 0; // internal count of how many proposals have been voted on; used to alternate inputs
	public int proposalCooldown = 0; // cooldown in ticks before the next proposal can be started

	private ProposalHandler(@NotNull Minecraft client) {
		this.client = client;
		twitchChat = new TwitchChat((user, message) -> client.execute(() -> {
			if (currentProposal != null)
				currentProposal.viewerVote(user, message);
		}));
	}

	public static @NotNull ProposalHandler get() {
		if (INSTANCE == null)
			INSTANCE = new ProposalHandler(Minecraft.getInstance());
		return INSTANCE;
	}

	public static @NotNull Optional<ProposalHandler> getOptional() {
		return Optional.ofNullable(INSTANCE);
	}

	public void joinChannel(@NotNull String channel) {
		twitchChat.join(channel);
		startNextProposal();
	}

	public void leaveChannel() {
		twitchChat.leave();
	}

	public @Nullable String getChannel() {
		return twitchChat.getChannel();
	}

	public boolean isActive() {
		return twitchChat.getChannel() != null;
	}

	public @NotNull Minecraft client() {
		return client;
	}

	public @Nullable ClientVoteStorage getProposalStorage() {
		if (client.player == null)
			return null;
		return client.player.connection.getVoteStorage();
	}

	public @Nullable ClientVoteStorage.VoteInfo getProposal(UUID id) {
		ClientVoteStorage storage = getProposalStorage();
		if (storage == null)
			return null;
		return storage.getVote(id);
	}

	public boolean canVote(UUID player, UUID proposal) {
		return canVote(player, getProposal(proposal));
	}

	public boolean canVote(UUID player, @Nullable ClientVoteStorage.VoteInfo proposal) {
		if (proposal == null)
			return false;
		if (proposal.proposalVoteCount(player).count() > 0)
			return false;
		return getRemainingTimeFor(proposal) > MIN_DURATION;
	}

	public long getRemainingTimeFor(UUID proposalId) {
		return getRemainingTimeFor(getProposal(proposalId));
	}

	public long getRemainingTimeFor(@Nullable ClientVoteStorage.VoteInfo proposal) {
		if (proposal == null)
			return 0;
		long worldTime = client.level == null ? 0L : client.level.getGameTime();
		return Math.max(0L, proposal.leftoverTicks(worldTime) - DURATION_PADDING);
	}

	public void startNextProposal() {
		if (currentProposal != null && !currentProposal.isClosed())
			return;
		if (proposalCooldown > 0)
			return;
		currentProposal = null;
		if (!isActive())
			return;
		ClientVoteStorage storage = getProposalStorage();
		if (storage == null)
			return;
		UUID player = Optional.ofNullable(client.player).map(Entity::getUUID).orElse(null);
		if (player == null)
			return;
		Map<UUID, ClientVoteStorage.VoteInfo> proposals = new HashMap<>();
		storage.visitVotes(proposals::put);
		currentProposal = proposals.entrySet()
				.stream()
				.filter(entry -> canVote(player, entry.getValue()))
				.map(entry -> Map.entry(entry.getKey(), getRemainingTimeFor(entry.getValue())))
				.min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.map(proposal -> new ProposalVote(this, proposal, proposalCount++))
				.orElse(null);
	}

	public @Nullable ProposalVote getCurrentProposal() {
		return currentProposal;
	}

	public void tick() {
		if (currentProposal != null)
			currentProposal.tick();
		if (--proposalCooldown <= 0)
			startNextProposal();
	}

	public void reset() {
		if (currentProposal != null)
			currentProposal.close();
		currentProposal = null;
		proposalCooldown = 0;
		proposalCount = 0;
		leaveChannel();
	}
}
