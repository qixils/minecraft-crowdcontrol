package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

class CommandSourceMapper<E extends CommandSource> implements EntityMapper<E> {
	protected final @NotNull SpongeCrowdControlPlugin plugin;

	public CommandSourceMapper(@NotNull SpongeCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	@NotNull
	public Audience asAudience(@NotNull CommandSource source) {
		if (source instanceof Player)
			return plugin.adventure().player((Player) source);
		return plugin.adventure().receiver(source);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull E entity) {
		if (entity instanceof Identifiable)
			return Optional.of(((Identifiable) entity).getUniqueId());
		return Optional.empty();
	}

	@Override
	public boolean isAdmin(@NotNull E commandSource) {
		return commandSource.hasPermission(Plugin.ADMIN_PERMISSION);
	}
}
