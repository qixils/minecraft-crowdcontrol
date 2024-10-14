package dev.qixils.crowdcontrol.common;

import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static dev.qixils.crowdcontrol.common.util.OptionalUtil.stream;

/**
 * Maps a {@link PublicEffectPayload} to the players that should receive it.
 *
 * @param <P> class used to represent online players
 */
public interface PlayerManager<P> {

	/**
	 * Gets the plugin that owns this PlayerManager.
	 *
	 * @return the plugin
	 */
	@CheckReturnValue
	@NotNull
	Plugin<P, ?> getPlugin();

	/**
	 * Determines whether a player can have effects applied to them.
	 *
	 * @param player player to test
	 * @param request initiating request for context
	 * @return whether effects can be applied or not
	 */
	boolean canApply(@NotNull P player, @Nullable PublicEffectPayload request);

	/**
	 * Determines whether a player is a spectator.
	 *
	 * @param player player to test
	 * @return player is spectator
	 */
	boolean isSpectator(@NotNull P player);

	/**
	 * Gets the raw list of players on the server, including spectators.
	 *
	 * @return full player list
	 */
	@CheckReturnValue
	@NotNull
	List<@NotNull P> getAllPlayersFull();

	/**
	 * Fetches all online players that should be affected by the provided {@link PublicEffectPayload}.
	 *
	 * @param request the request to be processed
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	default Stream<@NotNull P> getPlayers(final @NotNull PublicEffectPayload request) {
		if (getPlugin().isGlobal())
			return getAllPlayers(request);

		return stream(getPlugin().optionalCrowdControl())
			.flatMap(cc -> cc.getPlayerIds(request.getTarget().getId()).stream())
			.flatMap(uuid -> stream(getPlugin().playerMapper().getPlayer(uuid)))
			.filter(player -> canApply(player, request));
	}

	/**
	 * Fetches all online players that should be affected by global requests.
	 *
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	default Stream<@NotNull P> getAllPlayers(@Nullable PublicEffectPayload request) {
		return getAllPlayersFull().stream().filter(player -> canApply(player, request));
	}

	/**
	 * Gets all the spectators in the server.
	 *
	 * @return spectators
	 */
	@NotNull
	default Stream<@NotNull P> getSpectators() {
		return getAllPlayersFull().stream().filter(this::isSpectator);
	}
}
