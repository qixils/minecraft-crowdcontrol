package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class ServerCommandSourceMapper implements EntityMapper<ServerCommandSource> {
	private final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerCommandSource entity) {
		return plugin.adventure().audience(entity);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull ServerCommandSource entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUuid);
	}

	@Override
	public boolean isAdmin(@NotNull ServerCommandSource entity) {
		return entity.hasPermissionLevel(3);
	}
}
