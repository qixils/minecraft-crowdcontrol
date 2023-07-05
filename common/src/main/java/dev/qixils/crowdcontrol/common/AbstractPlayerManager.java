package dev.qixils.crowdcontrol.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.socket.Request;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Abstraction layer for {@link PlayerManager} which implements platform-agnostic methods.
 *
 * @param <P> class used to represent online players
 */
public abstract class AbstractPlayerManager<P> implements PlayerManager<P> {

	private final Multimap<String, UUID> twitchToUserMap =
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
	public @NotNull Set<UUID> getLinkedPlayers(@NotNull Request.Target target) {
		Set<UUID> uuids = new HashSet<>();
		if (target.getName() != null)
			uuids.addAll(twitchToUserMap.get(target.getName().toLowerCase(Locale.ENGLISH)));
		if (target.getLogin() != null)
			uuids.addAll(twitchToUserMap.get(target.getLogin().toLowerCase(Locale.ENGLISH)));

		CrowdControl cc = getPlugin().getCrowdControl();
		if (cc == null)
			return uuids;
		PlayerEntityMapper<P> mapper = getPlugin().playerMapper();

		for (Request.Source source : cc.getSources()) {
			if (!target.equalsRoughly(source.target())) {
				getPlugin().getSLF4JLogger().debug("Skipping source {} because it does not match target {}", source, target);
				continue;
			}

			P player = null;
			if (source.login() != null)
				player = mapper.getPlayerByLogin(source.login()).orElse(null);
			if (player == null && source.ip() != null && getPlugin().isAutoDetectIP())
				player = mapper.getPlayer(source.ip()).orElse(null);

			if (player != null) {
				getPlugin().getSLF4JLogger().debug("Found player {} from source {} (matches target {})", player, source, target);
				uuids.add(mapper.getUniqueId(player));
			} else {
				getPlugin().getSLF4JLogger().info("Failed to find player from source {}", source);
			}
		}

		return uuids;
	}

	@Override
	public @NotNull Collection<String> getLinkedAccounts(@NotNull UUID uuid) {
		// TODO: optimize
		return Multimaps.invertFrom(twitchToUserMap, HashMultimap.create(twitchToUserMap.size(), 1)).get(uuid);
	}
}
