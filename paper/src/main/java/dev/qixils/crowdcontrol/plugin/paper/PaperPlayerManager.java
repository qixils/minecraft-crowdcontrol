package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.plugin.paper.commands.GamemodeCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class PaperPlayerManager extends AbstractPlayerManager<Player> {
	private final PaperCrowdControlPlugin plugin;

	@Contract(value = "_ -> param1", mutates = "param1")
	private @NotNull List<@NotNull Player> filter(@NotNull List<Player> players) {
		players.removeIf(player -> player == null
				|| !player.isValid()
				|| player.isDead()
				|| (player.getGameMode() == GameMode.SPECTATOR && !GamemodeCommand.isEffectActive(plugin, player))
		);
		return players;
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getAllPlayers() {
		return filter(new ArrayList<>(Bukkit.getOnlinePlayers()));
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getPlayers(final @NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		List<Player> players = new ArrayList<>(request.getTargets().length);
		for (Target target : request.getTargets()) {
			for (UUID uuid : getLinkedPlayers(target.getName()))
				players.add(Bukkit.getPlayer(uuid));
		}

		return filter(players);
	}
}
