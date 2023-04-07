package dev.qixils.crowdcontrol.plugin.fabric.client;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import net.minecraft.class_8471;
import net.minecraft.world.World;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// TODO: overlay
// TODO: prevent streamer from voting themselves in the vanilla UI

public final class ProposalHandler {
	public final FabricPlatformClient plugin;
	private final TwitchChat twitchChat;
	public final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private ProposalVote currentProposal = null;
	private int proposalCount = 0; // internal count of how many proposals have been voted on; used to alternate inputs

	public ProposalHandler(FabricPlatformClient plugin) {
		this.plugin = plugin;
		twitchChat = TwitchChatBuilder.builder().build();
		twitchChat.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
			if (currentProposal != null)
				currentProposal.viewerVote(event.getUser().getId(), event.getMessage());
		});
	}

	public void joinChannel(String channel) {
		twitchChat.joinChannel(channel);
		startNextProposal();
	}

	public void leaveChannel(String channel) {
		twitchChat.leaveChannel(channel);
	}

	public class_8471 getProposalStorage() {
		return plugin.player().map(player -> player.networkHandler.method_51017()).orElse(null);
	}

	public class_8471.class_8474 getProposal(UUID id) {
		class_8471 storage = getProposalStorage();
		if (storage == null)
			return null;
		return storage.method_51074(id);
	}

	public Duration getRemainingTimeFor(UUID proposalId) {
		return getRemainingTimeFor(getProposal(proposalId));
	}

	public Duration getRemainingTimeFor(class_8471.class_8474 proposal) {
		if (proposal == null)
			return Duration.ZERO;
		long worldTime = plugin.client().map(client -> client.world).map(World::getTime).orElse(0L);
		long ticks = Math.max(0L, proposal.method_51079(worldTime));
		return Duration.ofMillis(ticks * 50L);
	}

	public void startNextProposal() {
		if (currentProposal != null && !currentProposal.isClosed())
			return;
		if (twitchChat.getChannels().isEmpty())
			return;
		class_8471 storage = getProposalStorage();
		if (storage == null)
			return;
		Map<UUID, Duration> proposals = new HashMap<>();
		storage.method_51072((id, proposal) -> proposals.put(id, getRemainingTimeFor(proposal)));
		UUID proposal = proposals.entrySet()
				.stream()
				.filter(entry -> entry.getValue().compareTo(ProposalVote.MIN_DURATION) >= 0)
				.min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse(null);
		if (proposal == null)
			return;
		currentProposal = new ProposalVote(this, proposal, proposalCount++);
	}
}
