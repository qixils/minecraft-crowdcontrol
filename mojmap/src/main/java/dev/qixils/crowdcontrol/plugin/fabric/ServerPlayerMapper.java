package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.LoginData;
import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static dev.qixils.crowdcontrol.common.Plugin.ADMIN_PERMISSION;

@Getter
@RequiredArgsConstructor
public class ServerPlayerMapper implements PlayerEntityMapper<ServerPlayer> {
	protected final ModdedCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return entity;
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull ServerPlayer entity) {
		return Optional.of(entity.getUUID());
	}

	@Override
	public @NotNull UUID getUniqueId(@NotNull ServerPlayer entity) {
		return entity.getUUID();
	}

	@Override
	public boolean isAdmin(@NotNull ServerPlayer entity) {
		if (entity.hasPermissions(3)) return true;
		if (plugin.getPermissionUtil().check(entity, ADMIN_PERMISSION)) return true;
		return PlayerEntityMapper.super.isAdmin(entity);
	}

	@Override
	public @NotNull String getUsername(@NotNull ServerPlayer player) {
		return player.getGameProfile().getName();
	}

	@Override
	public @NotNull Optional<ServerPlayer> getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(plugin.getServer()).map(server -> server.getPlayerList().getPlayer(uuid));
	}

	@SuppressWarnings("ConstantValue")
	@Override
	public @NotNull Optional<ServerPlayer> getPlayer(@NotNull InetAddress ip) {
		return Optional.ofNullable(plugin.getServer()).map(server -> {
			ServerPlayer result = null;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.connection == null)
					continue;
				SocketAddress address = player.connection.getRemoteAddress();
				if (!(address instanceof InetSocketAddress inetAddress))
					continue;
				if (!Objects.equals(inetAddress.getAddress(), ip))
					continue;

				// found match; check for duplicates
				if (result != null)
					return null;

				// ok
				result = player;
			}
			return result;
		});
	}

	@Override
	public @NotNull Optional<ServerPlayer> getPlayerByLogin(@NotNull LoginData login) {
		return Optional.ofNullable(plugin.getServer()).flatMap(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.getGameProfile().getName().equalsIgnoreCase(login.getName()) || player.getUUID().equals(login.getId()))
					return Optional.of(player);
			}
			return Optional.empty();
		});
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public @NotNull Optional<InetAddress> getIP(@NotNull ServerPlayer player) {
		return Optional.ofNullable(player.connection).map(connection -> {
			SocketAddress address = connection.getRemoteAddress();
			if (address instanceof InetSocketAddress inetAddress)
				return inetAddress.getAddress();
			return null;
		});
	}
}
