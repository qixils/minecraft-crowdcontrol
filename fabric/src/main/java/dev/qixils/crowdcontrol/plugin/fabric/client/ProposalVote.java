package dev.qixils.crowdcontrol.plugin.fabric.client;

import net.minecraft.class_8373;
import net.minecraft.class_8471;
import net.minecraft.util.math.MathHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProposalVote {
	public static final Duration MIN_DURATION = Duration.ofSeconds(20);
	public static final Duration MAX_DURATION = Duration.ofSeconds(60);
	private final Duration duration;
	public final UUID id;
	public final Instant endsAt; // TODO: remove and instead just update ticks remaining so it pauses with the game?
	private final ProposalHandler handler;
	private final SortedMap<String, OptionWrapper> options = new TreeMap<>();
	private final Map<String, String> votes = new HashMap<>();
	private boolean closed = false;

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

		Duration duration = handler.getRemainingTimeFor(vote);
		if (duration.compareTo(MIN_DURATION) < 0)
			throw new IllegalArgumentException("Proposal too short");
		else if (duration.compareTo(MAX_DURATION) > 0)
			duration = MAX_DURATION;
		this.duration = duration;
		Instant started = Instant.now();
		endsAt = started.plus(duration);
		handler.executor.schedule(this::voteForWinner, duration.toMillis(), TimeUnit.MILLISECONDS);
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
		return counts;
	}

	public int voteCount() {
		return votes.size();
	}

	public String getWinnerKey() {
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

	public OptionWrapper getWinner() {
		return options.get(getWinnerKey());
	}

	public void voteForWinner() {
		if (isClosed()) return;
		closed = true;
		try {
			// TODO: vote multiple times (if possible)?
			// TODO: bypass resources?
			handler.plugin.player().ifPresent(player -> player.networkHandler.method_51006(getWinner().id(), (i, optional) -> {}));
		} catch (Exception ignored) {}
		handler.executor.schedule(handler::startNextProposal, 10, TimeUnit.SECONDS);
	}

	public boolean isClosed() {
		return closed || getProposal() == null;
	}

	public Duration getRemainingTime() {
		return Duration.between(Instant.now(), endsAt);
	}

	public double getRemainingTimePercentage() {
		return MathHelper.clamp(getRemainingTime().toMillis() / (double) duration.toMillis(), 0, 1);
	}

	public class_8471.class_8474 getProposal() {
		return handler.getProposal(id);
	}

	public SortedMap<String, OptionWrapper> getOptions() {
		return Collections.unmodifiableSortedMap(options);
	}
}
