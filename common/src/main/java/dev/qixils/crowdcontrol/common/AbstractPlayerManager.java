package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Abstraction layer for {@link PlayerManager} which implements platform-agnostic methods.
 *
 * @param <P> class used to represent online players
 */
public abstract class AbstractPlayerManager<P> implements PlayerManager<P> {

	@NotNull
	protected Optional<PermissionWrapper> getEffectPermission(@Nullable PublicEffectPayload request) {
		if (request == null) return Optional.empty();
		String effect = request.getEffect().getEffectId();
		return Optional.of(PermissionWrapper.builder()
			.node("crowdcontrol.use." + effect.toLowerCase(Locale.US))
			.description("Whether a player is allowed to receive the " + effect + " effect")
			.defaultPermission(PermissionWrapper.DefaultPermission.ALL)
			.build());
	}
}
