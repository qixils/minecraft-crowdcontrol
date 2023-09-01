package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.socket.Request;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Maps a {@link Request} to the players that should receive it.
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
	 * Fetches all online players that should be affected by the provided {@link Request}.
	 *
	 * @param request the request to be processed
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	List<@NotNull P> getPlayers(final @NotNull Request request);

	/**
	 * Fetches all online players that should be affected by global requests.
	 *
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	List<@NotNull P> getAllPlayers();

	/**
	 * Gets all the spectators in the server.
	 *
	 * @return spectators
	 */
	@NotNull
	Collection<@NotNull P> getSpectators();

	/**
	 * Links a stream account to a Minecraft account. Account links are not exclusive.
	 *
	 * @param uuid the UUID of the Minecraft account
	 * @param username the streamer's username
	 * @return whether a new link was created
	 */
	boolean linkPlayer(@NotNull UUID uuid, @NotNull String username);

	/**
	 * Unlinks a stream account from a Minecraft account.
	 *
	 * @param uuid the UUID of the Minecraft account
	 * @param username the streamer's username
	 * @return whether a link was deleted
	 */
	boolean unlinkPlayer(@NotNull UUID uuid, @NotNull String username);

	/**
	 * Returns the stream accounts linked to a Minecraft account.
	 *
	 * @param uuid the UUID of the Minecraft account
	 * @return a collection of stream accounts
	 */
	@CheckReturnValue
	@NotNull
	Collection<@NotNull String> getLinkedAccounts(@NotNull UUID uuid);

	/**
	 * Returns the Minecraft accounts linked to a Target.
	 *
	 * @param target the target
	 * @return a collection of Minecraft accounts
	 */
	@CheckReturnValue
	@NotNull
	Set<@NotNull UUID> getLinkedPlayers(Request.@NotNull Target target);
}
