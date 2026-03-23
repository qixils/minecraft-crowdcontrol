package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.PlayerEntityMapper;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ServerPlayerMapper implements PlayerEntityMapper<ServerPlayer> {
	protected final ModdedCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull ServerPlayer entity) {
		return plugin.adventure().audience(entity);
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
	public boolean hasPermission(@NotNull ServerPlayer entity, @NotNull PermissionWrapper perm) {
		return plugin.getPermissionUtil().check(entity, perm);
	}

	@Override
	public @NotNull String getUsername(@NotNull ServerPlayer player) {
		return player.getGameProfile().name();
	}

	@Override
	public @NotNull Optional<ServerPlayer> getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(plugin.getServer()).map(server -> server.getPlayerList().getPlayer(uuid));
	}

}
