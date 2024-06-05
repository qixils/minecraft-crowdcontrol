package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.plugin.paper.commands.GameModeCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.*;

@Getter
@RequiredArgsConstructor
public final class PaperPlayerManager extends AbstractPlayerManager<Player> {
	private final PaperCrowdControlPlugin plugin;

	@Contract(value = "_, _ -> param1", mutates = "param1")
	private @NotNull List<@NotNull Player> filter(@NotNull List<Player> players, @Nullable Request request) {
		Permission perm = getEffectPermission(request).map(PaperUtil::toPaper).orElse(null);
		players.removeIf(player -> player == null
			|| !player.isValid()
			|| player.isDead()
			|| (player.getGameMode() == GameMode.SPECTATOR && !GameModeCommand.isEffectActive(plugin, player))
			|| !player.hasPermission(PaperUtil.USE_PERMISSION)
			|| (perm != null && !player.hasPermission(perm))
		);
		return players;
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getAllPlayers() {
		return filter(new ArrayList<>(Bukkit.getOnlinePlayers()), null);
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getPlayers(final @NotNull Request request) {
		if (plugin.isGlobal(request))
			return getAllPlayers();

		Set<UUID> uuids = new HashSet<>(request.getTargets().length);
		for (Target target : request.getTargets())
			uuids.addAll(getLinkedPlayers(target));

		List<Player> players = new ArrayList<>(uuids.size());
		for (UUID uuid : uuids)
			players.add(Bukkit.getPlayer(uuid));

		return filter(players, request);
	}

	@Override
	public @NotNull Collection<Player> getSpectators() {
		List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		players.removeIf(player -> player.getGameMode() != GameMode.SPECTATOR
			|| GameModeCommand.isEffectActive(plugin, player));
		return players;
	}
}
