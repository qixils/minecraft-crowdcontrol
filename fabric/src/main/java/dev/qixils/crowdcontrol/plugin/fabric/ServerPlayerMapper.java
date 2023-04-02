package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ServerPlayerMapper implements PlayerEntityMapper<ServerPlayerEntity> {
	protected final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayerEntity entity) {
		return entity;
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull ServerPlayerEntity entity) {
		return Optional.of(entity.getUuid());
	}

	@Override
	public @NotNull UUID getUniqueId(@NotNull ServerPlayerEntity entity) {
		return entity.getUuid();
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayerEntity entity) {
		if (entity.hasPermissionLevel(3)) return true;
		return PlayerEntityMapper.super.isAdmin(entity);
	}

	@Override
	public @NotNull String getUsername(@NotNull ServerPlayerEntity player) {
		return player.getGameProfile().getName();
	}

	@Override
	public @NotNull Optional<ServerPlayerEntity> getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(plugin.getServer()).map(server -> server.getPlayerManager().getPlayer(uuid));
	}
}
