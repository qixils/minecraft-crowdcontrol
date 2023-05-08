package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.PlayerConnection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Objects;
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
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull Player entity) {
		return Optional.of(entity.getUniqueId());
	}

	@Override
	public @NotNull UUID getUniqueId(@NotNull Player entity) {
		return entity.getUniqueId();
	}

	@Override
	public @NotNull String getUsername(@NotNull Player player) {
		return player.getName();
	}

	@Override
	public @NotNull Optional<Player> getPlayer(@NotNull UUID uuid) {
		return plugin.getGame().getServer().getPlayer(uuid);
	}

	@Override
	public @NotNull Optional<Locale> getLocale(@NotNull Player entity) {
		return Optional.of(entity.getLocale());
	}

	@SuppressWarnings("ConstantValue")
	@Override
	public @NotNull Optional<Player> getPlayer(@NotNull InetAddress ip) {
		Player result = null;
		for (Player player : plugin.getGame().getServer().getOnlinePlayers()) {
			PlayerConnection connection = player.getConnection();
			if (connection == null)
				continue;
			InetSocketAddress address = connection.getAddress();
			if (address == null)
				continue;
			if (!Objects.equals(address.getAddress(), ip))
				continue;

			// found match; check for duplicates
			if (result != null)
				return Optional.empty();

			// ok
			result = player;
		}
		return Optional.ofNullable(result);
	}
}
