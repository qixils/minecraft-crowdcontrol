package dev.qixils.crowdcontrol.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

/**
 * Abstraction layer for {@link PlayerMapper} which implements platform-agnostic methods.
 *
 * @param <P> class used to represent online players
 */
public abstract class AbstractPlayerMapper<P> implements PlayerMapper<P> {

	protected final Multimap<String, UUID> twitchToUserMap =
			Multimaps.synchronizedSetMultimap(HashMultimap.create(1, 1));

	@Override
	public boolean linkPlayer(@NotNull UUID uuid, @NotNull String twitchUsername) {
		return twitchToUserMap.put(twitchUsername.toLowerCase(Locale.ENGLISH), uuid);
	}

	@Override
	public boolean unlinkPlayer(@NotNull UUID uuid, @NotNull String twitchUsername) {
		return twitchToUserMap.remove(twitchUsername.toLowerCase(Locale.ENGLISH), uuid);
	}

	@Override
	public @NotNull Collection<@NotNull UUID> getLinkedPlayers(@NotNull String twitchUsername) {
		return twitchToUserMap.get(twitchUsername.toLowerCase(Locale.ENGLISH));
	}

	@Override
	public @NotNull Collection<@NotNull String> getLinkedAccounts(@NotNull UUID uuid) {
		return Multimaps.invertFrom(twitchToUserMap, HashMultimap.create(twitchToUserMap.size(), 1)).get(uuid);
	}
}
