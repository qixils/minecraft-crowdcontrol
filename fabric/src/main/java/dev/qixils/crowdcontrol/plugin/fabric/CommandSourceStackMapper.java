package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
public class CommandSourceStackMapper implements EntityMapper<ServerCommandSource> {
	protected final FabricCrowdControlPlugin plugin;

	protected CommandSourceStackMapper(FabricCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull Audience asAudience(@NotNull ServerCommandSource entity) {
		return plugin.adventure().audience(entity);
	}

	@Override
	public final @NotNull Optional<UUID> tryGetUniqueId(@NotNull ServerCommandSource entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUuid).or(() -> entity.get(Identity.UUID));
	}

	@Override
	public final boolean isAdmin(@NotNull ServerCommandSource entity) {
		if (entity.hasPermissionLevel(3)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
