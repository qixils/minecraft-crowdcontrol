package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.AllArgsConstructor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class FabricPlayerManager extends AbstractPlayerManager<ServerPlayerEntity> {
	private final FabricCrowdControlPlugin plugin;

	@Contract(value = "_ -> param1", mutates = "param1")
	private @NotNull List<@NotNull ServerPlayerEntity> filter(@NotNull List<ServerPlayerEntity> players) {
		players.removeIf(player -> player == null
						|| player.isRemoved()
						|| player.isDead()
						|| player.notInAnyWorld
						|| player.getHealth() <= 0
//				|| player.isSpectator() && !player.get(GAME_MODE_EFFECT).isPresent()) // TODO
		);
		return players;
	}

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getAllPlayers() {
		return filter(new ArrayList<>(plugin.server().getPlayerManager().getPlayerList()));
	}

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getPlayers(@NotNull Request request) {
		if (plugin.isGlobal())
			return getAllPlayers();

		List<ServerPlayerEntity> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target.getName()))
				// null values are filtered out later
				players.add(plugin.server().getPlayerManager().getPlayer(uuid));
		}

		return filter(players);
	}
}
