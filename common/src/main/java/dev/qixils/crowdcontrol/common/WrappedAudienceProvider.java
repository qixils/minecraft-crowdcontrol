package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * An audience provider that wraps another audience provider.
 */
public interface WrappedAudienceProvider extends AudienceProvider {

	/**
	 * Wraps the given audience.
	 *
	 * @param audience the audience to wrap
	 * @return the wrapped audience
	 */
	@NotNull
	@ApiStatus.OverrideOnly
	Audience wrap(@NotNull Audience audience);

	/**
	 * The wrapped audience provider.
	 *
	 * @return the wrapped audience provider
	 */
	@NotNull
	@ApiStatus.OverrideOnly
	AudienceProvider provider();

	/**
	 * Wraps the given audiences.
	 *
	 * @param audiences the audiences to wrap
	 * @return the wrapped audiences
	 */
	@NotNull
	default Audience wrap(@NotNull Collection<? extends Audience> audiences) {
		return audiences.stream().map(this::wrap).collect(Audience.toAudience());
	}

	@Override
	@NotNull
	default Audience all() {
		return wrap(provider().all());
	}

	@Override
	@NotNull
	default Audience console() {
		return wrap(provider().console());
	}

	@Override
	@NotNull
	default Audience players() {
		return wrap(provider().players());
	}

	@Override
	@NotNull
	default Audience player(@NotNull UUID playerId) {
		return wrap(provider().player(playerId));
	}

	@Override
	@NotNull
	default Audience permission(@NotNull String permission) {
		return wrap(provider().permission(permission));
	}

	@Override
	@NotNull
	default Audience world(@NotNull Key world) {
		return wrap(provider().world(world));
	}

	@Override
	@NotNull
	default Audience server(@NotNull String serverName) {
		return wrap(provider().server(serverName));
	}

	@Override
	@NotNull
	default Audience permission(@NotNull Key permission) {
		return wrap(provider().permission(permission));
	}

	@Override
	default void close() {
		provider().close();
	}

	@Override
	@NotNull
	default ComponentFlattener flattener() {
		return provider().flattener();
	}
}
