package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil.check;

@Getter
@AllArgsConstructor
public class MojmapPlayerManager extends AbstractPlayerManager<ServerPlayer> {
	private final FabricCrowdControlPlugin plugin;

	@Contract(value = "_, _ -> param1", mutates = "param1")
	private @NotNull List<@NotNull ServerPlayer> filter(@NotNull List<ServerPlayer> players, @Nullable Request request) {
		PermissionWrapper perm = getEffectPermission(request).orElse(null);
		players.removeIf(player -> player == null
						|| player.isDeadOrDying()
						|| (player.isSpectator() && player.cc$getGameTypeEffect() == null)
						|| !check(player, Plugin.USE_PERMISSION)
						|| (perm != null && !check(player, perm))
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getAllPlayers() {
		return filter(new ArrayList<>(plugin.server().getPlayerList().getPlayers()), null);
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		List<ServerPlayer> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target))
				// null values are filtered out later
				players.add(plugin.server().getPlayerList().getPlayer(uuid));
		}

		return filter(players, request);
	}

	@Override
	public @NotNull Collection<@NotNull ServerPlayer> getSpectators() {
		List<ServerPlayer> players = new ArrayList<>(plugin.server().getPlayerList().getPlayers());
		players.removeIf(player -> !player.isSpectator() || player.cc$getGameTypeEffect() != null);
		return players;
	}
}
