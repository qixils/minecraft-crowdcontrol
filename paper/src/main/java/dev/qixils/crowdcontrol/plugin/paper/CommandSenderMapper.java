package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

class CommandSenderMapper<E extends CommandSender> implements EntityMapper<E> {
	@Override
	public @NotNull Audience asAudience(@NotNull E entity) {
		return entity;
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull E entity) {
		if (entity instanceof Entity)
			return Optional.of(((Entity) entity).getUniqueId());
		return EntityMapper.super.getUniqueId(entity);
	}

	@Override
	public boolean isAdmin(@NotNull E commandSource) {
		return commandSource.hasPermission(Plugin.ADMIN_PERMISSION) || commandSource.isOp();
	}
}
