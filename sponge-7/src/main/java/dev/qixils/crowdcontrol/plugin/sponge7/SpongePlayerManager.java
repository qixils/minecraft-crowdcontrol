package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.sponge7.data.entity.GameModeEffectData;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SpongePlayerManager extends AbstractPlayerManager<Player> {
	private final SpongeCrowdControlPlugin plugin;

	@Contract(value = "_, _ -> param1", mutates = "param1")
	private @NotNull List<@NotNull Player> filter(@NotNull List<Player> players, @Nullable Request request) {
		PermissionWrapper perm = getEffectPermission(request).orElse(null);
		players.removeIf(player -> player == null
			|| player.isRemoved()
			|| !player.isOnline()
			|| !player.isLoaded()
			|| player.health().get() <= 0.0
			|| (player.gameMode().get().equals(GameModes.SPECTATOR) && !player.get(GameModeEffectData.class).isPresent())
			|| !SpongeUtil.hasPermission(player, Plugin.USE_PERMISSION)
			|| (perm != null && !SpongeUtil.hasPermission(player, perm))
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull Player> getAllPlayers() {
		return filter(new ArrayList<>(plugin.getGame().getServer().getOnlinePlayers()), null);
	}

	@Override
	public @NotNull List<@NotNull Player> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		Server server = plugin.getGame().getServer();
		List<Player> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target))
				players.add(server.getPlayer(uuid).orElse(null));
		}

		return filter(players, request);
	}

	@Override
	public @NotNull Collection<@NotNull Player> getSpectators() {
		List<Player> players = new ArrayList<>(plugin.getGame().getServer().getOnlinePlayers());
		players.removeIf(player -> !player.gameMode().get().equals(GameModes.SPECTATOR)
			|| player.get(GameModeEffectData.class).isPresent()
		);
		return players;
	}
}
