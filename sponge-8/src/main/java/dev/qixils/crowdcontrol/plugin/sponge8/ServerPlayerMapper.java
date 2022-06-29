package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
class ServerPlayerMapper implements EntityMapper<ServerPlayer> {
	protected final SpongeCrowdControlPlugin plugin;

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
		if (entity.hasPermission(Plugin.ADMIN_PERMISSION)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
