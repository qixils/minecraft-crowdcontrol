package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

class CommandCauseMapper implements EntityMapper<CommandCause> {
	@Override
	public @NotNull Audience asAudience(@NotNull CommandCause entity) {
		return entity.audience();
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull CommandCause entity) {
		Subject subject = entity.subject();
		if (subject instanceof Identifiable)
			return Optional.ofNullable(((Identifiable) subject).uniqueId());
		return EntityMapper.super.getUniqueId(entity);
	}

	@Override
	public boolean isAdmin(@NotNull CommandCause entity) {
		return entity.hasPermission(Plugin.ADMIN_PERMISSION);
	}
}
