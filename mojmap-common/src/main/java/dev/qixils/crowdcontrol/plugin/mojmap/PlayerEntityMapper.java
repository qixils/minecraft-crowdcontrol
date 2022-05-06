package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PlayerEntityMapper implements EntityMapper<ServerPlayer> {
	private final MojmapPlugin<?> plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return plugin.adventure().player(entity.getUUID());
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull ServerPlayer entity) {
		return Optional.of(entity.getUUID());
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayer entity) {
		return entity.hasPermissions(3);
	}
}
