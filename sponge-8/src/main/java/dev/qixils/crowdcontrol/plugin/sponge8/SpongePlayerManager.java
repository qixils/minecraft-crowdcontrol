package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.SpongeUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin.GAME_MODE_EFFECT;

@Getter
@RequiredArgsConstructor
public class SpongePlayerManager extends AbstractPlayerManager<ServerPlayer> {
	private final SpongeCrowdControlPlugin plugin;

	@Contract(value = "_, _ -> param1", mutates = "param1")
	private @NotNull List<@NotNull ServerPlayer> filter(@NotNull List<ServerPlayer> players, @Nullable Request request) {
		PermissionWrapper perm = getEffectPermission(request).orElse(null);
		players.removeIf(player -> player == null
			|| player.isRemoved()
			|| !player.isOnline()
			|| !player.isLoaded()
			|| player.health().get() <= 0.0
			|| (player.gameMode().get().equals(GameModes.SPECTATOR.get()) && !player.get(GAME_MODE_EFFECT).isPresent())
			|| !SpongeUtil.hasPermission(player, Plugin.USE_PERMISSION)
			|| (perm != null && !SpongeUtil.hasPermission(player, perm))
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getAllPlayers() {
		return filter(new ArrayList<>(plugin.getGame().server().onlinePlayers()), null);
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		Server server = plugin.getGame().server();
		List<ServerPlayer> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target))
				players.add(server.player(uuid).orElse(null));
		}

		return filter(players, request);
	}

	@Override
	public @NotNull Collection<@NotNull ServerPlayer> getSpectators() {
		List<ServerPlayer> players = new ArrayList<>(plugin.getGame().server().onlinePlayers());
		players.removeIf(player -> !player.gameMode().get().equals(GameModes.SPECTATOR.get())
			|| player.get(GAME_MODE_EFFECT).isPresent());
		return players;
	}
}
