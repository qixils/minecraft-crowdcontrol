package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
class CommandSourceMapper<E extends CommandSource> implements EntityMapper<E> {
	protected final @NotNull SpongeCrowdControlPlugin plugin;

	@Override
	@NotNull
	public Audience asAudience(@NotNull CommandSource source) {
		if (source instanceof Player)
			return plugin.adventure().player((Player) source);
		return plugin.adventure().receiver(source);
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull E entity) {
		if (entity instanceof Identifiable)
			return Optional.of(((Identifiable) entity).getUniqueId());
		return Optional.empty();
	}

	@Override
	public boolean isAdmin(@NotNull E commandSource) {
		if (commandSource.hasPermission(Plugin.ADMIN_PERMISSION)) return true;
		return EntityMapper.super.isAdmin(commandSource);
	}
}
