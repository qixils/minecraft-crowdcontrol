package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.identity.Identity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.net.InetAddress;
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

	/**
	 * Fetches the unique ID of an entity.
	 *
	 * @param entity the entity to fetch the UUID of
	 * @return the UUID of the entity
	 */
	@CheckReturnValue
	default @NotNull UUID getUniqueId(@NotNull P entity) {
		return tryGetUniqueId(entity).orElseThrow(() ->
				new UnsupportedOperationException("Player object does not support UUID"));
	}

	/**
	 * Gets an online player connected with the provided IP address.
	 *
	 * @param ip the IP address of the player
	 * @return the player with the given IP address, or empty if not found
	 */
	@CheckReturnValue
	@NotNull Optional<P> getPlayer(@NotNull InetAddress ip);

	/**
	 * Gets an online player connected with the provided login name or id.
	 *
	 * @param login the login name or id of the player
	 * @return the player with the given login name or id, or empty if not found
	 */
	@CheckReturnValue
	default @NotNull Optional<P> getPlayerByLogin(@NotNull String login) {
		return getPlayerByLogin(new LoginData(login));
	}

	/**
	 * Gets an online player connected with the provided login.
	 *
	 * @param login the login of the player
	 * @return the player with the given login, or empty if not found
	 */
	@CheckReturnValue
	@NotNull Optional<P> getPlayerByLogin(@NotNull LoginData login);

	/**
	 * Gets the IP address of a player.
	 *
	 * @param player the player to get the IP address of
	 * @return the IP address of the player
	 */
	@CheckReturnValue
	@NotNull Optional<InetAddress> getIP(@NotNull P player);
}
