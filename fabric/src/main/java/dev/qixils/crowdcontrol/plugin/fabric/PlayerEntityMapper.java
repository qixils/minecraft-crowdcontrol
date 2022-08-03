package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PlayerEntityMapper implements EntityMapper<ServerPlayer> {
	protected final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return entity;
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull ServerPlayer entity) {
		return Optional.of(entity.getUUID());
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayer entity) {
		if (entity.hasPermissions(3)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
