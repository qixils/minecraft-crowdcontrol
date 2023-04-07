package dev.qixils.crowdcontrol.plugin.fabric.client;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import net.minecraft.class_8471;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// TODO: prevent streamer from voting themselves in the vanilla UI

public final class ProposalHandler {
	public final FabricPlatformClient plugin;
	public final ProposalHud overlay = new ProposalHud(this);
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

	public boolean canVote(UUID player, UUID proposal) {
		return canVote(player, getProposal(proposal));
	}

	public boolean canVote(UUID player, class_8471.class_8474 proposal) {
		if (proposal == null)
			return false;
		if (!proposal.method_51080(player).method_51075()) // TODO: skip if any votes have been cast
			return false;
		return getRemainingTimeFor(proposal).compareTo(ProposalVote.MIN_DURATION) >= 0;
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
		UUID player = plugin.player().map(Entity::getUuid).orElse(null);
		if (player == null)
			return;
		Map<UUID, class_8471.class_8474> proposals = new HashMap<>();
		storage.method_51072((id, proposal) -> proposals.put(id, getProposal(id)));
		currentProposal = proposals.entrySet()
				.stream()
				.filter(entry -> canVote(player, entry.getKey()))
				.map(entry -> Map.entry(entry.getKey(), getRemainingTimeFor(entry.getValue())))
				.min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.map(proposal -> new ProposalVote(this, proposal, proposalCount++))
				.orElse(null);
	}

	public @Nullable ProposalVote getCurrentProposal() {
		return currentProposal;
	}
}
