package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

class PlayerMapper<E extends Player> extends CommandSourceMapper<E> implements PlayerEntityMapper<E> {
	public PlayerMapper(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Audience asAudience(@NotNull E entity) {
		return plugin.adventure().player(entity);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull E entity) {
		return Optional.of(entity.getUniqueId());
	}

	@Override
	public @NotNull String getUsername(@NotNull E player) {
		return player.getName();
	}
}
