package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.AllArgsConstructor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class MojmapPlayerManager extends AbstractPlayerManager<ServerPlayerEntity> {
	private final FabricCrowdControlPlugin plugin;

	@Contract(value = "_ -> param1", mutates = "param1")
	private @NotNull List<@NotNull ServerPlayerEntity> filter(@NotNull List<ServerPlayerEntity> players) {
		players.removeIf(player -> player == null
						|| player.isDead()
						|| (player.isSpectator() && Components.GAME_TYPE_EFFECT.get(player).getValue() == null)
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getAllPlayers() {
		return filter(new ArrayList<>(plugin.server().getPlayerManager().getPlayerList()));
	}

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		List<ServerPlayerEntity> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target))
				// null values are filtered out later
				players.add(plugin.server().getPlayerManager().getPlayer(uuid));
		}

		return filter(players);
	}

	@Override
	public @NotNull Collection<@NotNull ServerPlayerEntity> getSpectators() {
		List<ServerPlayerEntity> players = new ArrayList<>(plugin.server().getPlayerManager().getPlayerList());
		players.removeIf(player -> !player.isSpectator()
				|| Components.GAME_TYPE_EFFECT.get(player).getValue() != null);
		return players;
	}
}
