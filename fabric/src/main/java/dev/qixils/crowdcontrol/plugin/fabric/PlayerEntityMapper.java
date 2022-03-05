package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PlayerEntityMapper implements EntityMapper<ServerPlayerEntity> {
	private final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayerEntity entity) {
		return plugin.adventure().audience(entity);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull ServerPlayerEntity entity) {
		return Optional.of(entity.getUuid());
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayerEntity entity) {
		return entity.hasPermissionLevel(3);
	}
}
