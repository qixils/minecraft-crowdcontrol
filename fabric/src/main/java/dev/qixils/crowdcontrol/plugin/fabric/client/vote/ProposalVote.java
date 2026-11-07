package dev.qixils.crowdcontrol.plugin.fabric.client.vote;

import dev.qixils.crowdcontrol.plugin.fabric.vote.VoteNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.voting.votes.OptionId;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class ProposalVote {
	public static final int MIN_DURATION = 20 * 20;
	public static final int MAX_DURATION = 50 * 20;
	public static final int COOLDOWN = 7 * 20;
	/** Ticks to subtract from the time remaining on a proposal to ensure our vote gets sent on time */
	public static final int DURATION_PADDING = 2;
	public final UUID id;
	private final long originalDuration;
	private long ticksLeft;
	private final ProposalHandler handler;
	private final SortedMap<String, OptionWrapper> options = new TreeMap<>();
	private final Map<String, String> votes = new HashMap<>();
	private boolean closed = false;
	private boolean pendingFinishedPacket = false;
	private @Nullable ClientVoteStorage.VoteInfo proposalCache = null;
	private @Nullable String topOptionKey = null;
	public final boolean isNumbered;

	public ProposalVote(ProposalHandler handler, UUID id, int streamProposalCount) {
		this.id = id;
		this.handler = handler;

		ClientVoteStorage.VoteInfo vote = getProposal();
		if (vote == null)
			throw new IllegalArgumentException("Invalid proposal ID");

		List<OptionWrapper> optionList = vote.voteInfo().options().entrySet().stream().sorted(Map.Entry.comparingByKey(OptionId.COMPARATOR)).map(OptionWrapper::new).toList();
		isNumbered = streamProposalCount % 2 == 0;
		for (int i = 0; i < optionList.size(); i++) {
			OptionWrapper option = optionList.get(i);
			String key = isNumbered
					? String.valueOf(i + 1)
					: String.valueOf((char) ('A' + i));
			options.put(key, option);
		}

		long duration = handler.getRemainingTimeFor(vote);
		if (duration < MIN_DURATION)
			throw new IllegalArgumentException("Proposal too short");
		else if (duration > MAX_DURATION)
			duration = MAX_DURATION;
		this.originalDuration = duration;
		this.ticksLeft = duration;

		// play sound effect
		LocalPlayer player = handler.client().player;
		if (player != null)
			player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.MASTER, /*volume:*/ 1.2f, /*pitch:*/ 1.2f);
	}

	public void tick() {
		if (pendingFinishedPacket) {
			// let the server know that we're done so that it can enact the proposal early
			pendingFinishedPacket = false;
			if (ClientPlayNetworking.canSend(VoteNetworking.VOTED_ID)) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				buf.writeUUID(id);
				ClientPlayNetworking.send(VoteNetworking.VOTED_ID, buf);
			}
			return;
		}
		if (closed) return;
		if (--ticksLeft <= 0)
			voteForWinner();
	}

	public void viewerVote(String userId, String vote) {
		if (closed) return;
		String[] words = vote.trim().toUpperCase(Locale.ENGLISH).split(" ");
		if (words.length == 0) return;
		if (options.containsKey(words[0]))
			votes.put(userId, words[0]);
	}

	public Map<String, Integer> voteCounts() {
		Map<String, Integer> counts = new HashMap<>(options.size());
		for (String key : options.keySet())
			counts.put(key, 0);
		for (String key : votes.values())
			counts.put(key, counts.get(key) + 1);
		return Collections.unmodifiableMap(counts);
	}

	public int voteCount() {
		return votes.size();
	}

	public String getTopOptionKey() {
		if (topOptionKey != null)
			return topOptionKey;
		int max = 0;
		String maxKey = options.lastKey(); // 'Do nothing'
		for (Map.Entry<String, Integer> entry : voteCounts().entrySet()) {
			int count = entry.getValue();
			if (count > max) {
				max = count;
				maxKey = entry.getKey();
			}
		}
		// TODO: support TieStrategy
		return maxKey;
	}

	public OptionWrapper getTopOption() {
		return options.get(getTopOptionKey());
	}

	public void voteForWinner() {
		if (isClosed()) return;
		closed = true;
		handler.proposalCooldown = COOLDOWN;
		proposalCache = getProposal();
		topOptionKey = getTopOptionKey();
		LocalPlayer player = handler.client().player;
		if (player == null || proposalCache == null) return;
		try {
			ClientVoteStorage.VoteInfo proposal = proposalCache;
			int voteLimit = Math.min(
					// total limit of votes
					proposal.proposalVoteCount(player.getUUID()).limit().orElse(1),
					// limit of votes per option
					options.values().stream()
							.mapToInt(option -> proposal.optionVoteCount(player.getUUID(), option.id()).limit().orElse(1))
							.min().orElse(1)
			);
			Map<String, Integer> viewerVotes = voteCounts();
			int totalViewerVotes = viewerVotes.values().stream().mapToInt(Integer::intValue).sum();
			// distribute votes proportionally to viewer votes
			int votesUsed = 0;
			voteloop:
			{
				// TODO: TieStrategy
				for (Map.Entry<String, Integer> entry : viewerVotes.entrySet()) {
					String key = entry.getKey();
					int count = entry.getValue();
					int votes = (int) Math.round(count / (double) totalViewerVotes * voteLimit);
					for (int i = 0; i < votes; i++) {
						player.connection.voteFor(options.get(key).id(), (j, optional) -> {
						});
						if (++votesUsed >= voteLimit)
							break voteloop;
					}
				}
			}
			pendingFinishedPacket = true;
		} catch (Exception ignored) {
		}
	}

	public void close() {
		// force close
		closed = true;
	}

	public boolean isClosed() {
		return closed || getProposal() == null;
	}

	public Duration getRemainingTime() {
		return Duration.ofMillis(ticksLeft * 50L);
	}

	public long getRemainingTicks() {
		return ticksLeft;
	}

	public double getRemainingTimePercentage() {
		return Mth.clamp(ticksLeft / (double) originalDuration, 0, 1);
	}

	public @Nullable ClientVoteStorage.VoteInfo getProposal() {
		if (proposalCache != null)
			return proposalCache;
		return handler.getProposal(id);
	}

	public SortedMap<String, OptionWrapper> getOptions() {
		return Collections.unmodifiableSortedMap(options);
	}
}
