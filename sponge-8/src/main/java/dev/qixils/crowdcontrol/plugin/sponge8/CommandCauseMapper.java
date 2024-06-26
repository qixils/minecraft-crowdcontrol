package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.SpongeUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
class CommandCauseMapper implements EntityMapper<CommandCause> {
	protected final SpongeCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull CommandCause entity) {
		return entity.audience();
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull CommandCause entity) {
		Subject subject = entity.subject();
		if (subject instanceof Identifiable)
			return Optional.ofNullable(((Identifiable) subject).uniqueId());
		return EntityMapper.super.tryGetUniqueId(entity);
	}

	@Override
	public boolean isAdmin(@NotNull CommandCause entity) {
		if (SpongeUtil.hasPermission(entity, Plugin.ADMIN_PERMISSION)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
