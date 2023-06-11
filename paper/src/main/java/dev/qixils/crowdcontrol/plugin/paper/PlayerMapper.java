package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.LoginData;
import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

class PlayerMapper extends CommandSenderMapper<Player> implements PlayerEntityMapper<Player> {
	public PlayerMapper(PaperCrowdControlPlugin plugin) {
		super(plugin);
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
		return Optional.ofNullable(plugin.getServer().getPlayer(uuid));
	}

	@Override
	public @NotNull Optional<Locale> getLocale(@NotNull Player entity) {
		return Optional.of(entity.locale());
	}

	@Override
	public @NotNull Optional<Player> getPlayer(@NotNull InetAddress ip) {
		Player result = null;
		for (Player player : Bukkit.getOnlinePlayers()) {
			InetSocketAddress address = player.getAddress();
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

	@Override
	public @NotNull Optional<Player> getPlayerByLogin(@NotNull LoginData login) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().equalsIgnoreCase(login.getName()) || player.getUniqueId().equals(login.getId()))
				return Optional.of(player);
		}
		return Optional.empty();
	}

	@Override
	public @NotNull Optional<InetAddress> getIP(@NotNull Player player) {
		return Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress);
	}
}
