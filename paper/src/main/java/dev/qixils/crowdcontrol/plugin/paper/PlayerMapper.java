package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

class PlayerMapper extends CommandSenderMapper<Player> implements PlayerEntityMapper<Player> {
	public PlayerMapper(PaperCrowdControlPlugin plugin) {
		super(plugin);
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
		return Optional.ofNullable(plugin.getServer().getPlayer(uuid));
	}

	@Override
	public @NotNull Optional<Locale> getLocale(@NotNull Player entity) {
		return Optional.of(entity.locale());
	}
}
