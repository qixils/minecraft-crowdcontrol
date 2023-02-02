package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

class PlayerMapper extends CommandSourceMapper<Player> implements PlayerEntityMapper<Player> {
	public PlayerMapper(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Audience asAudience(@NotNull Player entity) {
		return plugin.adventure().player(entity);
	}

	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull Player entity) {
		return Optional.of(entity.getUniqueId());
	}

	@Override
	public @NotNull String getUsername(@NotNull Player player) {
		return player.getName();
	}

	@Override
	public @NotNull Optional<Player> getPlayer(@NotNull UUID uuid) {
		return plugin.getGame().getServer().getPlayer(uuid);
	}
}
