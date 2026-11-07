package dev.qixils.crowdcontrol.plugin.fabric.vote;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCEventType;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.UserToken;
import live.crowdcontrol.cc4j.websocket.payload.ProfileType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/** Server voting dispatchers & listeners */
public final class VoteNetworking {
	/** Serverbound packet casts chat's vote on a proposal */
	public static final ResourceLocation VOTED_ID = new ResourceLocation("crowdcontrol", "voted");
	/** Serverbound packet stating the Twitch channel to read from or null */
	public static final ResourceLocation CHANNEL_ID = new ResourceLocation("crowdcontrol", "vote_channel");

	// map of proposal UUIDs to a set of player UUIDs who voted on it
	private static final Map<UUID, Set<UUID>> PLAYER_VOTES = new HashMap<>();

	private VoteNetworking() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(VOTED_ID, (server, player, handler, buf, responseSender) -> {
			UUID id = buf.readUUID();
			server.execute(() -> onVoted(server, player, id));
		});
		// the client may not have been able to receive the channel when the account was first loaded
		S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
			if (!channels.contains(CHANNEL_ID)) return;
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			ServerPlayer player = handler.getPlayer();
			FabricCrowdControlPlugin.getInstance().optionalCCPlayer(player.getUUID())
				.ifPresent(ccPlayer -> server.execute(() -> sendChannel(player, getChannel(ccPlayer))));
		});
	}

	private static void onVoted(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @NotNull UUID id) {
		if (server.getVoteStorage().getPendingVotesIds().noneMatch(id::equals)) {
			PLAYER_VOTES.remove(id);
			return;
		}
		Set<UUID> voters = PLAYER_VOTES.computeIfAbsent(id, $ -> new HashSet<>());
		voters.add(player.getUUID());
		if (server.getPlayerList().getPlayers().stream().allMatch(online -> voters.contains(online.getUUID()))) {
			// everyone has voted, so there's no need to make them wait for the timer
			PLAYER_VOTES.remove(id);
			server.finishVote(id, true);
		}
	}

	/**
	 * Gets the player's Twitch chat channel from their linked account
	 */
	public static @NotNull Optional<String> getChannel(@NotNull CCPlayer ccPlayer) {
		UserToken token = ccPlayer.getUserToken();
		if (token == null || token.getProfile() != ProfileType.TWITCH)
			return Optional.empty();
		return Optional.ofNullable(token.getName()).map(name -> name.toLowerCase(Locale.ROOT));
	}

	public static void sendChannel(@NotNull ServerPlayer player, @NotNull Optional<String> channel) {
		if (!ServerPlayNetworking.canSend(player, CHANNEL_ID)) return;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(channel.orElse(""));
		ServerPlayNetworking.send(player, CHANNEL_ID, buf);
	}

	/**
	 * Initializes event listeners for a new streamer
	 */
	public static void trackAccount(@NotNull FabricCrowdControlPlugin plugin, @NotNull ServerPlayer joiningPlayer) {
		UUID uuid = joiningPlayer.getUUID();
		plugin.optionalCCPlayer(uuid).ifPresent(ccPlayer -> {
			Runnable update = () -> plugin.getSyncExecutor().execute(() -> {
				ServerPlayer player = plugin.server().getPlayerList().getPlayer(uuid);
				if (player != null) sendChannel(player, getChannel(ccPlayer));
			});
			ccPlayer.getEventManager().registerEventRunnable(CCEventType.AUTHENTICATED, update);
			ccPlayer.getEventManager().registerEventRunnable(CCEventType.UNAUTHENTICATED, update);
			ccPlayer.getEventManager().registerEventRunnable(CCEventType.AUTH_EXPIRED, update);
			update.run();
		});
	}

	/**
	 * Whether the player has a connected Twitch chat
	 */
	public static boolean isChatVoting(@NotNull ServerPlayer player) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return false;
		var plugin = FabricCrowdControlPlugin.getInstance();
		if (plugin.getModVersion(player).isEmpty()) return false;
		return plugin.optionalCCPlayer(player.getUUID()).flatMap(VoteNetworking::getChannel).isPresent();
	}
}
