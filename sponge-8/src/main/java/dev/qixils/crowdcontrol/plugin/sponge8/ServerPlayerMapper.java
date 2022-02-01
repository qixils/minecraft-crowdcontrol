package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

class ServerPlayerMapper implements EntityMapper<ServerPlayer> {
	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return entity;
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull ServerPlayer entity) {
		return Optional.ofNullable(entity.uniqueId());
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayer entity) {
		return entity.hasPermission(Plugin.ADMIN_PERMISSION);
	}
}
