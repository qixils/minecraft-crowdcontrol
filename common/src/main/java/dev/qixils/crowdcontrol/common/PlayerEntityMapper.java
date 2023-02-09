package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.identity.Identity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.Optional;
import java.util.UUID;

/**
 * An implementation-agnostic wrapper which fetches data from implementation-specific players
 * or converts them to other data types.
 *
 * @param <P> type of player class being wrapped
 */
public interface PlayerEntityMapper<P> extends EntityMapper<P> {

	/**
	 * Gets the username of a player.
	 *
	 * @param player the player to get the username of
	 * @return the username of the player
	 */
	@CheckReturnValue
	default @NotNull String getUsername(@NotNull P player) {
		return asAudience(player).get(Identity.NAME).orElseThrow(() ->
				new UnsupportedOperationException("Player object does not support Identity.NAME"));
	}

	/**
	 * Gets the player with the given UUID.
	 *
	 * @param uuid the UUID of the player
	 * @return the player with the given UUID, or empty if not found
	 */
	@CheckReturnValue
	@NotNull Optional<P> getPlayer(@NotNull UUID uuid);
}
