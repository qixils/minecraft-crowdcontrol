package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
class ServerPlayerMapper implements PlayerEntityMapper<ServerPlayer> {
	protected final SpongeCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return plugin.translator().wrap(entity);
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull ServerPlayer entity) {
		return Optional.of(entity.uniqueId());
	}

	@Override
	public @NotNull UUID getUniqueId(@NotNull ServerPlayer entity) {
		return entity.uniqueId();
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayer entity) {
		if (entity.hasPermission(Plugin.ADMIN_PERMISSION)) return true;
		return PlayerEntityMapper.super.isAdmin(entity);
	}

	@Override
	public @NotNull String getUsername(@NotNull ServerPlayer player) {
		return player.name();
	}

	@Override
	public @NotNull Optional<ServerPlayer> getPlayer(@NotNull UUID uuid) {
		return plugin.getGame().server().player(uuid);
	}

	@Override
	public @NotNull Optional<Locale> getLocale(@NotNull ServerPlayer entity) {
		return Optional.of(entity.locale());
	}
}
