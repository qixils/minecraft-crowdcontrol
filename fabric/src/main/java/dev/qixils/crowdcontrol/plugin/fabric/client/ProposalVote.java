package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.class_8373;
import net.minecraft.class_8471;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.time.Duration;
import java.util.*;

public class ProposalVote {
	public static final int MIN_DURATION = 20 * 20;
	public static final int MAX_DURATION = 50 * 20;
	public static final int COOLDOWN = 7 * 20;
	/**
	 * How many ticks to subtract from the time remaining on a proposal to ensure our vote gets sent on time.
	 */
	public static final int DURATION_PADDING = 2;
	public final UUID id;
	private final long originalDuration;
	private long ticksLeft;
	private final ProposalHandler handler;
	private final SortedMap<String, OptionWrapper> options = new TreeMap<>();
	private final Map<String, String> votes = new HashMap<>();
	private boolean closed = false;
	private boolean pendingFinishedPacket = false;
	private class_8471.class_8474 proposalCache = null;
	private String topOptionKey = null;

	public ProposalVote(ProposalHandler handler, UUID id, int streamProposalCount) {
		this.id = id;
		this.handler = handler;

		class_8471.class_8474 vote = getProposal();
		if (vote == null)
			throw new IllegalArgumentException("Invalid proposal ID");

		List<OptionWrapper> optionList = vote.method_51082().comp_1358().entrySet().stream().sorted(Map.Entry.comparingByKey(class_8373.field_43985)).map(OptionWrapper::new).toList();
		for (int i = 0; i < optionList.size(); i++) {
			OptionWrapper option = optionList.get(i);
			String key = streamProposalCount % 2 == 0
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
		FabricPlatformClient.getOptional()
				.flatMap(FabricPlatformClient::player)
				.ifPresent(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.comp_349(), SoundCategory.MASTER, /*volume:*/ 1.2f, /*pitch:*/ 1.2f));
	}

	public void tick() {
		if (pendingFinishedPacket) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeUuid(id);
			ClientPlayNetworking.send(FabricCrowdControlPlugin.VOTED_ID, buf);
			pendingFinishedPacket = false;
			return;
		}
		if (closed) return;
		if (--ticksLeft <= 0)
			voteForWinner();
	}

	public void viewerVote(String userId, String vote) {
		if (closed) return;
		vote = vote.toUpperCase(Locale.ENGLISH).split(" ")[0];
		if (options.containsKey(vote))
			votes.put(userId, vote);
	}

	public Map<String, Integer> voteCounts() {
		Map<String, Integer> counts = new HashMap<>(options.keySet().size());
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
		try {
			handler.plugin.player().ifPresent(player -> {
				var proposal = getProposal();
				int voteLimit = Math.min(
						// total limit of votes
						proposal.method_51080(player.getUuid()).comp_1454().orElse(1),
						// limit of votes per option
						options.values().stream()
								.mapToInt(option -> proposal.method_51081(player.getUuid(), option.id()).comp_1454().orElse(1))
								.min().orElse(1)
				);
				Map<String, Integer> viewerVotes = voteCounts();
				int totalViewerVotes = viewerVotes.values().stream().mapToInt(Integer::intValue).sum();
				// distribute votes proportionally to viewer votes
				int votesUsed = 0;
				voteloop: {
					// TODO: TieStrategy
					for (Map.Entry<String, Integer> entry : viewerVotes.entrySet()) {
						String key = entry.getKey();
						int count = entry.getValue();
						int votes = (int) Math.round(count / (double) totalViewerVotes * voteLimit);
						for (int i = 0; i < votes; i++) {
							player.networkHandler.method_51006(options.get(key).id(), (j, optional) -> {});
							if (++votesUsed >= voteLimit)
								break voteloop;
						}
					}
				}
				pendingFinishedPacket = true;
			});
		} catch (Exception ignored) {}
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
		return MathHelper.clamp(ticksLeft / (double) originalDuration, 0, 1);
	}

	public class_8471.class_8474 getProposal() {
		if (proposalCache != null)
			return proposalCache;
		return handler.getProposal(id);
	}

	public SortedMap<String, OptionWrapper> getOptions() {
		return Collections.unmodifiableSortedMap(options);
	}
}
