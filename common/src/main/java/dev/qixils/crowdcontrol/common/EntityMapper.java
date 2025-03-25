package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * An implementation-agnostic wrapper which fetches data from implementation-specific entity classes
 * or converts them to other data types.
 *
 * @param <E> type of class being wrapped
 */
public interface EntityMapper<E> {

	/**
	 * Gets the {@link Plugin} that this mapper is associated with.
	 *
	 * @return the plugin
	 */
	Plugin<?, ?> getPlugin();

	/**
	 * Converts an entity to an adventure {@link Audience}.
	 *
	 * @param entity the entity to convert
	 * @return adventure audience
	 */
	@NotNull
	default Audience asAudience(@NotNull E entity) {
		if (entity instanceof Audience)
			return (Audience) entity;
		throw new UnsupportedOperationException("#asAudience is unsupported");
	}

	/**
	 * Converts a collection of entities to an adventure {@link Audience}.
	 *
	 * @param entities collection of entities to convert
	 * @return adventure audience
	 */
	@NotNull
	default Audience asAudience(@NotNull Collection<@NotNull E> entities) {
		return entities.stream().map(this::asAudience).collect(Audience.toAudience());
	}

	/**
	 * Fetches the unique ID of an entity.
	 *
	 * @param entity the entity to fetch the UUID of
	 * @return the UUID of the entity or empty if unavailable
	 */
	@CheckReturnValue
	default @NotNull Optional<UUID> tryGetUniqueId(@NotNull E entity) {
		return asAudience(entity).get(Identity.UUID);
	}

	/**
	 * Determines if the provided entity is an administrator. This is defined as the object having
	 * the {@link Plugin#ADMIN_PERMISSION} permission node or being a Minecraft operator.
	 *
	 * @param entity the entity to check
	 * @return true if the entity is an administrator
	 */
	default boolean isAdmin(@NotNull E entity) {
		Plugin plugin = getPlugin();
		Object player = null;
		try {
			player = plugin.asPlayer(entity);
		} catch (Exception ignored) {
		}
		if (player == null) return false;
		return plugin.globalEffectsUsableFor(player);
	}

	/**
	 * Fetches the locale of an entity.
	 *
	 * @param entity the entity to fetch the locale of
	 * @return the locale of the entity
	 */
	@CheckReturnValue
	default @NotNull Optional<Locale> getLocale(@NotNull E entity) {
		return asAudience(entity).get(Identity.LOCALE);
	}
}
