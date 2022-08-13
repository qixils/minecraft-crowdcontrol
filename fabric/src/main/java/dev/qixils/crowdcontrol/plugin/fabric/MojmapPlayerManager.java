package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.PlayerData;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class MojmapPlayerManager extends AbstractPlayerManager<ServerPlayer> {
	private final FabricCrowdControlPlugin plugin;

	@Contract(value = "_ -> param1", mutates = "param1")
	private @NotNull List<@NotNull ServerPlayer> filter(@NotNull List<ServerPlayer> players) {
		players.removeIf(player -> player == null
						|| player.isRemoved()
						|| player.isDeadOrDying()
						|| player.getHealth() <= 0
						|| (player.isSpectator() && ((PlayerData) player).gameModeEffect() == null)
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getAllPlayers() {
		return filter(new ArrayList<>(plugin.server().getPlayerList().getPlayers()));
	}

	@Override
	public @NotNull List<@NotNull ServerPlayer> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal())
			return getAllPlayers();

		List<ServerPlayer> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target.getName()))
				// null values are filtered out later
				players.add(plugin.server().getPlayerList().getPlayer(uuid));
		}

		return filter(players);
	}

	@Override
	public @NotNull Collection<@NotNull ServerPlayer> getSpectators() {
		List<ServerPlayer> players = new ArrayList<>(plugin.server().getPlayerList().getPlayers());
		players.removeIf(player -> !player.isSpectator()
				|| ((PlayerData) player).gameModeEffect() != null);
		return players;
	}
}
