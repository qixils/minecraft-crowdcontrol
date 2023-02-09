package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
class CommandSenderMapper<E extends CommandSender> implements EntityMapper<E> {
	protected final PaperCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull E entity) {
		return plugin.translator().wrap(entity);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull E entity) {
		if (entity instanceof Entity)
			return Optional.of(((Entity) entity).getUniqueId());
		return EntityMapper.super.getUniqueId(entity);
	}

	@Override
	public boolean isAdmin(@NotNull E commandSource) {
		if (commandSource.hasPermission(Plugin.ADMIN_PERMISSION) || commandSource.isOp()) return true;
		return EntityMapper.super.isAdmin(commandSource);
	}
}
